package com.soas.currencyconversion.service;

import com.soas.servicelibrary.dto.BankAccountDto;
import com.soas.servicelibrary.dto.TransactionResponse;
import com.soas.servicelibrary.enums.Role;
import com.soas.servicelibrary.proxy.BankAccountProxy;
import com.soas.servicelibrary.proxy.CurrencyExchangeProxy;
import com.soas.util.exception.InsufficientFundsException;
import com.soas.util.exception.InvalidRequestException;
import com.soas.util.exception.UnauthorizedRoleException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CurrencyConversionService {

    private static final int FIAT_SCALE = 2;

    private final BankAccountProxy bankAccountProxy;
    private final CurrencyExchangeProxy currencyExchangeProxy;

    public CurrencyConversionService(BankAccountProxy bankAccountProxy,
                                     CurrencyExchangeProxy currencyExchangeProxy) {
        this.bankAccountProxy = bankAccountProxy;
        this.currencyExchangeProxy = currencyExchangeProxy;
    }

    public TransactionResponse convert(String from, String to, BigDecimal quantity,
                                       Role caller, String callerEmail) {
        if (caller != Role.USER) {
            throw new UnauthorizedRoleException("Razmenu valuta moze da izvrsi samo korisnik sa ulogom USER");
        }
        if (callerEmail == null || callerEmail.isBlank()) {
            throw new UnauthorizedRoleException("Nije poznato ko salje zahtev");
        }
        return convertForUser(callerEmail, from, to, quantity);
    }

    public TransactionResponse convertForUser(String email, String from, String to, BigDecimal quantity) {
        String source = normalize(from);
        String target = normalize(to);

        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidRequestException("Kolicina za razmenu mora da bude veca od nule");
        }
        if (source.equals(target)) {
            throw new InvalidRequestException("Polazna i ciljna valuta ne mogu da budu iste");
        }

        BigDecimal amountToSpend = quantity.setScale(FIAT_SCALE, RoundingMode.HALF_UP);
        ensureFunds(email, source, amountToSpend);

        BigDecimal rate = currencyExchangeProxy.getExchangeValue(source, target).getConversionMultiple();
        BigDecimal received = amountToSpend.multiply(rate).setScale(FIAT_SCALE, RoundingMode.HALF_UP);

        bankAccountProxy.changeAmount(email, source, amountToSpend.negate());
        bankAccountProxy.changeAmount(email, target, received);

        List<BankAccountDto> accounts = bankAccountProxy.getAccountsInternal(email);
        String message = "Uspesno je izvrsena razmena " + source + ": " + amountToSpend.toPlainString()
                + " za " + target + ": " + received.toPlainString();

        return new TransactionResponse(accounts, null, message);
    }

    private void ensureFunds(String email, String currency, BigDecimal needed) {
        BankAccountDto account = bankAccountProxy.getAccountsInternal(email).stream()
                .filter(a -> a.getCurrencyCode().equalsIgnoreCase(currency))
                .findFirst()
                .orElseThrow(() -> new InsufficientFundsException(
                        "Korisnik " + email + " ne poseduje valutu " + currency));

        if (account.getAmount().compareTo(needed) < 0) {
            throw new InsufficientFundsException("Nedovoljno sredstava: na racunu ima "
                    + account.getAmount().toPlainString() + " " + currency
                    + ", a potrebno je " + needed.toPlainString());
        }
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("Kod valute je obavezan");
        }
        return code.trim().toUpperCase();
    }
}
