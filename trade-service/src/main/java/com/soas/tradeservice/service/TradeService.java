package com.soas.tradeservice.service;

import com.soas.servicelibrary.dto.BankAccountDto;
import com.soas.servicelibrary.dto.CryptoWalletDto;
import com.soas.servicelibrary.dto.TransactionResponse;
import com.soas.servicelibrary.enums.Role;
import com.soas.servicelibrary.proxy.BankAccountProxy;
import com.soas.servicelibrary.proxy.CryptoWalletProxy;
import com.soas.servicelibrary.proxy.CurrencyConversionProxy;
import com.soas.tradeservice.client.RateClient;
import com.soas.util.exception.InsufficientFundsException;
import com.soas.util.exception.InvalidRequestException;
import com.soas.util.exception.UnauthorizedRoleException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class TradeService {

    private static final int FIAT_SCALE = 2;
    private static final int CRYPTO_SCALE = 8;
    // crypto se kupuje i prodaje iskljucivo za dolar i evro
    private static final Set<String> BASE_CURRENCIES = Set.of("EUR", "USD");

    private final RateClient rateClient;
    private final BankAccountProxy bankAccountProxy;
    private final CryptoWalletProxy cryptoWalletProxy;
    private final CurrencyConversionProxy currencyConversionProxy;

    public TradeService(RateClient rateClient,
                        BankAccountProxy bankAccountProxy,
                        CryptoWalletProxy cryptoWalletProxy,
                        CurrencyConversionProxy currencyConversionProxy) {
        this.rateClient = rateClient;
        this.bankAccountProxy = bankAccountProxy;
        this.cryptoWalletProxy = cryptoWalletProxy;
        this.currencyConversionProxy = currencyConversionProxy;
    }

    public TransactionResponse trade(String from, String to, BigDecimal quantity,
                                     Role caller, String callerEmail) {
        if (caller != Role.USER) {
            throw new UnauthorizedRoleException("Trgovinu moze da izvrsi samo korisnik sa ulogom USER");
        }
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new UnauthorizedRoleException("Nije poznato ko salje zahtev");
        }

        String source = normalize(from);
        String target = normalize(to);

        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidRequestException("Kolicina za razmenu mora da bude veca od nule");
        }
        if (source.equals(target)) {
            throw new InvalidRequestException("Polazna i ciljna valuta ne mogu da budu iste");
        }

        boolean sourceIsFiat = rateClient.isFiat(source);
        boolean targetIsFiat = rateClient.isFiat(target);

        if (sourceIsFiat && targetIsFiat) {
            throw new InvalidRequestException(
                    "Razmena dve fiat valute se izvrsava kroz currency-conversion servis");
        }
        if (sourceIsFiat) {
            return fiatToCrypto(callerEmail, source, target, quantity);
        }
        if (targetIsFiat) {
            return cryptoToFiat(callerEmail, source, target, quantity);
        }
        return cryptoToCrypto(callerEmail, source, target, quantity);
    }

    private TransactionResponse cryptoToCrypto(String email, String from, String to, BigDecimal quantity) {
        BigDecimal amount = quantity.setScale(CRYPTO_SCALE, RoundingMode.HALF_UP);
        ensureWalletFunds(email, from, amount);

        BigDecimal rate = rateClient.cryptoRate(from, to);
        BigDecimal received = amount.multiply(rate).setScale(CRYPTO_SCALE, RoundingMode.HALF_UP);

        cryptoWalletProxy.changeAmount(email, from, amount.negate());
        cryptoWalletProxy.changeAmount(email, to, received);

        String message = "Uspesno je izvrsena razmena " + from + ": " + amount.toPlainString()
                + " za " + to + ": " + received.toPlainString();
        return new TransactionResponse(null, cryptoWalletProxy.getWalletsInternal(email), message);
    }

    private TransactionResponse fiatToCrypto(String email, String from, String to, BigDecimal quantity) {
        BigDecimal amount = quantity.setScale(FIAT_SCALE, RoundingMode.HALF_UP);
        String base = BASE_CURRENCIES.contains(from) ? from : "EUR";
        BigDecimal baseAmount;

        if (base.equals(from)) {
            ensureAccountFunds(email, from, amount);
            baseAmount = amount;
        } else {
            // valuta koja nije EUR ni USD se prvo menja kroz currency-conversion servis
            ensureAccountFunds(email, from, amount);
            BigDecimal beforeBase = balanceOf(bankAccountProxy.getAccountsInternal(email), base);
            TransactionResponse converted = currencyConversionProxy.convertForUser(email, from, base, amount);
            BigDecimal afterBase = balanceOf(converted.getBankAccount(), base);
            baseAmount = afterBase.subtract(beforeBase);
        }

        BigDecimal rate = rateClient.cryptoRate(base, to);
        BigDecimal received = baseAmount.multiply(rate).setScale(CRYPTO_SCALE, RoundingMode.HALF_UP);

        bankAccountProxy.changeAmount(email, base, baseAmount.negate());
        cryptoWalletProxy.changeAmount(email, to, received);

        String message = "Uspesno je izvrsena razmena " + from + ": " + amount.toPlainString()
                + " za " + to + ": " + received.toPlainString()
                + (base.equals(from) ? "" : " (preko " + base + ")");
        return new TransactionResponse(null, cryptoWalletProxy.getWalletsInternal(email), message);
    }

    private TransactionResponse cryptoToFiat(String email, String from, String to, BigDecimal quantity) {
        BigDecimal amount = quantity.setScale(CRYPTO_SCALE, RoundingMode.HALF_UP);
        ensureWalletFunds(email, from, amount);

        String base = BASE_CURRENCIES.contains(to) ? to : "EUR";
        BigDecimal rate = rateClient.cryptoRate(from, base);
        BigDecimal baseAmount = amount.multiply(rate).setScale(FIAT_SCALE, RoundingMode.HALF_UP);

        cryptoWalletProxy.changeAmount(email, from, amount.negate());
        bankAccountProxy.changeAmount(email, base, baseAmount);

        List<BankAccountDto> accounts;
        BigDecimal finalAmount = baseAmount;

        if (base.equals(to)) {
            accounts = bankAccountProxy.getAccountsInternal(email);
        } else {
            // dobijeni EUR se dalje menja u trazenu valutu kroz currency-conversion servis
            BigDecimal beforeTarget = balanceOf(bankAccountProxy.getAccountsInternal(email), to);
            TransactionResponse converted = currencyConversionProxy.convertForUser(email, base, to, baseAmount);
            accounts = converted.getBankAccount();
            finalAmount = balanceOf(accounts, to).subtract(beforeTarget);
        }

        String message = "Uspesno je izvrsena razmena " + from + ": " + amount.toPlainString()
                + " za " + to + ": " + finalAmount.toPlainString()
                + (base.equals(to) ? "" : " (preko " + base + ")");
        return new TransactionResponse(accounts, null, message);
    }

    private void ensureAccountFunds(String email, String currency, BigDecimal needed) {
        BigDecimal available = balanceOf(bankAccountProxy.getAccountsInternal(email), currency);
        if (available.compareTo(needed) < 0) {
            throw new InsufficientFundsException("Nedovoljno sredstava: na racunu ima "
                    + available.toPlainString() + " " + currency
                    + ", a potrebno je " + needed.toPlainString());
        }
    }

    private void ensureWalletFunds(String email, String crypto, BigDecimal needed) {
        BigDecimal available = cryptoWalletProxy.getWalletsInternal(email).stream()
                .filter(w -> w.getCryptoCode().equalsIgnoreCase(crypto))
                .map(CryptoWalletDto::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
        if (available.compareTo(needed) < 0) {
            throw new InsufficientFundsException("Nedovoljno sredstava: u novcaniku ima "
                    + available.toPlainString() + " " + crypto
                    + ", a potrebno je " + needed.toPlainString());
        }
    }

    private BigDecimal balanceOf(List<BankAccountDto> accounts, String currency) {
        if (accounts == null) {
            return BigDecimal.ZERO;
        }
        return accounts.stream()
                .filter(a -> a.getCurrencyCode().equalsIgnoreCase(currency))
                .map(BankAccountDto::getAmount)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("Kod valute je obavezan");
        }
        return code.trim().toUpperCase();
    }
}
