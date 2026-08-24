package com.soas.cryptowallet.service;

import com.soas.cryptowallet.entity.CryptoWallet;
import com.soas.cryptowallet.repository.CryptoWalletRepository;
import com.soas.servicelibrary.dto.CryptoWalletDto;
import com.soas.servicelibrary.enums.Role;
import com.soas.util.exception.DuplicateResourceException;
import com.soas.util.exception.InsufficientFundsException;
import com.soas.util.exception.InvalidRequestException;
import com.soas.util.exception.ResourceNotFoundException;
import com.soas.util.exception.UnauthorizedRoleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CryptoWalletService {

    private static final String STARTING_CRYPTO = "ETH";

    private final CryptoWalletRepository repository;

    public CryptoWalletService(CryptoWalletRepository repository) {
        this.repository = repository;
    }

    public List<CryptoWalletDto> findAll(Role caller) {
        requireAdmin(caller);
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public List<CryptoWalletDto> findByEmail(String email, Role caller, String callerEmail) {
        requireAccessTo(email, caller, callerEmail);
        List<CryptoWallet> accounts = repository.findByEmail(email);
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Ne postoji novcanik za korisnika " + email);
        }
        return accounts.stream().map(this::toDto).toList();
    }

    public CryptoWalletDto findOne(String email, String cryptoCode, Role caller, String callerEmail) {
        requireAccessTo(email, caller, callerEmail);
        return toDto(getOrThrow(email, cryptoCode));
    }

    @Transactional
    public CryptoWalletDto create(CryptoWalletDto dto, Role caller) {
        requireAdmin(caller);
        if (dto.getEmail() == null || dto.getCryptoCode() == null) {
            throw new InvalidRequestException("Email adresa i kod kripto valute su obavezni");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() < 0) {
            throw new InvalidRequestException("Kolicina ne moze da bude negativna");
        }
        String currency = dto.getCryptoCode().toUpperCase();
        repository.findByEmailAndCryptoCode(dto.getEmail(), currency).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "Korisnik " + dto.getEmail() + " vec ima novcanik za kripto valutu " + currency);
        });
        return toDto(repository.save(new CryptoWallet(dto.getEmail(), currency, dto.getAmount())));
    }

    @Transactional
    public CryptoWalletDto update(Long id, CryptoWalletDto dto, Role caller) {
        requireAdmin(caller);
        CryptoWallet account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novcanik sa id " + id + " ne postoji"));
        if (dto.getAmount() != null) {
            if (dto.getAmount().signum() < 0) {
                throw new InvalidRequestException("Kolicina ne moze da bude negativna");
            }
            account.setAmount(dto.getAmount());
        }
        if (dto.getCryptoCode() != null) {
            account.setCryptoCode(dto.getCryptoCode().toUpperCase());
        }
        return toDto(repository.save(account));
    }

    @Transactional
    public void delete(Long id, Role caller) {
        requireAdmin(caller);
        CryptoWallet account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Novcanik sa id " + id + " ne postoji"));
        repository.delete(account);
    }

    // ---- pozivi koji stizu od drugih mikroservisa preko Feign-a ----

    @Transactional
    public CryptoWalletDto createStartingWallet(String email) {
        return repository.findByEmailAndCryptoCode(email, STARTING_CRYPTO)
                .map(this::toDto)
                .orElseGet(() -> toDto(repository.save(
                        new CryptoWallet(email, STARTING_CRYPTO, BigDecimal.ZERO))));
    }

    @Transactional
    public void deleteWalletsForUser(String email) {
        repository.deleteByEmail(email);
    }

    @Transactional
    public CryptoWalletDto changeAmount(String email, String cryptoCode, BigDecimal delta) {
        String currency = cryptoCode.toUpperCase();
        CryptoWallet account = repository.findByEmailAndCryptoCode(email, currency)
                .orElseGet(() -> {
                    if (delta.signum() < 0) {
                        throw new InsufficientFundsException(
                                "Korisnik " + email + " ne poseduje kripto valutu " + currency);
                    }
                    return new CryptoWallet(email, currency, BigDecimal.ZERO);
                });

        BigDecimal result = account.getAmount().add(delta);
        if (result.signum() < 0) {
            throw new InsufficientFundsException("Nedovoljno sredstava: u novcaniku ima "
                    + account.getAmount() + " " + currency + ", a potrebno je " + delta.abs());
        }
        account.setAmount(result);
        return toDto(repository.save(account));
    }

    // ---- provere ovlascenja ----

    private void requireAdmin(Role caller) {
        if (caller != Role.ADMIN) {
            throw new UnauthorizedRoleException("Ovu operaciju moze da izvrsi samo ADMIN");
        }
    }

    private void requireAccessTo(String email, Role caller, String callerEmail) {
        if (caller == Role.ADMIN) {
            return;
        }
        if (caller == Role.USER && email.equalsIgnoreCase(callerEmail)) {
            return;
        }
        if (caller == Role.USER) {
            throw new UnauthorizedRoleException("USER moze da pregleda samo svoj novcanik");
        }
        throw new UnauthorizedRoleException("OWNER nije autorizovan za koriscenje crypto-wallet servisa");
    }

    private CryptoWallet getOrThrow(String email, String cryptoCode) {
        return repository.findByEmailAndCryptoCode(email, cryptoCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Korisnik " + email + " nema novcanik za kripto valutu " + cryptoCode.toUpperCase()));
    }

    private CryptoWalletDto toDto(CryptoWallet account) {
        return new CryptoWalletDto(account.getId(), account.getEmail(),
                account.getCryptoCode(), account.getAmount());
    }
}
