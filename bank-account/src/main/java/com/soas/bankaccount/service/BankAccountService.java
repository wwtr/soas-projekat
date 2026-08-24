package com.soas.bankaccount.service;

import com.soas.bankaccount.entity.BankAccount;
import com.soas.bankaccount.repository.BankAccountRepository;
import com.soas.servicelibrary.dto.BankAccountDto;
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
public class BankAccountService {

    private static final String STARTING_CURRENCY = "EUR";

    private final BankAccountRepository repository;

    public BankAccountService(BankAccountRepository repository) {
        this.repository = repository;
    }

    public List<BankAccountDto> findAll(Role caller) {
        requireAdmin(caller);
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public List<BankAccountDto> findByEmail(String email, Role caller, String callerEmail) {
        requireAccessTo(email, caller, callerEmail);
        List<BankAccount> accounts = repository.findByEmail(email);
        if (accounts.isEmpty()) {
            throw new ResourceNotFoundException("Ne postoji bankovni racun za korisnika " + email);
        }
        return accounts.stream().map(this::toDto).toList();
    }

    public BankAccountDto findOne(String email, String currencyCode, Role caller, String callerEmail) {
        requireAccessTo(email, caller, callerEmail);
        return toDto(getOrThrow(email, currencyCode));
    }

    @Transactional
    public BankAccountDto create(BankAccountDto dto, Role caller) {
        requireAdmin(caller);
        if (dto.getEmail() == null || dto.getCurrencyCode() == null) {
            throw new InvalidRequestException("Email adresa i kod valute su obavezni");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() < 0) {
            throw new InvalidRequestException("Kolicina ne moze da bude negativna");
        }
        String currency = dto.getCurrencyCode().toUpperCase();
        repository.findByEmailAndCurrencyCode(dto.getEmail(), currency).ifPresent(existing -> {
            throw new DuplicateResourceException(
                    "Korisnik " + dto.getEmail() + " vec ima racun za valutu " + currency);
        });
        return toDto(repository.save(new BankAccount(dto.getEmail(), currency, dto.getAmount())));
    }

    @Transactional
    public BankAccountDto update(Long id, BankAccountDto dto, Role caller) {
        requireAdmin(caller);
        BankAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Racun sa id " + id + " ne postoji"));
        if (dto.getAmount() != null) {
            if (dto.getAmount().signum() < 0) {
                throw new InvalidRequestException("Kolicina ne moze da bude negativna");
            }
            account.setAmount(dto.getAmount());
        }
        if (dto.getCurrencyCode() != null) {
            account.setCurrencyCode(dto.getCurrencyCode().toUpperCase());
        }
        return toDto(repository.save(account));
    }

    @Transactional
    public void delete(Long id, Role caller) {
        requireAdmin(caller);
        BankAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Racun sa id " + id + " ne postoji"));
        repository.delete(account);
    }

    // ---- pozivi koji stizu od drugih mikroservisa preko Feign-a ----

    @Transactional
    public BankAccountDto createStartingAccount(String email) {
        return repository.findByEmailAndCurrencyCode(email, STARTING_CURRENCY)
                .map(this::toDto)
                .orElseGet(() -> toDto(repository.save(
                        new BankAccount(email, STARTING_CURRENCY, BigDecimal.ZERO))));
    }

    @Transactional
    public void deleteAccountsForUser(String email) {
        repository.deleteByEmail(email);
    }

    @Transactional
    public BankAccountDto changeAmount(String email, String currencyCode, BigDecimal delta) {
        String currency = currencyCode.toUpperCase();
        BankAccount account = repository.findByEmailAndCurrencyCode(email, currency)
                .orElseGet(() -> {
                    if (delta.signum() < 0) {
                        throw new InsufficientFundsException(
                                "Korisnik " + email + " ne poseduje valutu " + currency);
                    }
                    return new BankAccount(email, currency, BigDecimal.ZERO);
                });

        BigDecimal result = account.getAmount().add(delta);
        if (result.signum() < 0) {
            throw new InsufficientFundsException("Nedovoljno sredstava: na racunu ima "
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
            throw new UnauthorizedRoleException("USER moze da pregleda samo svoj racun");
        }
        throw new UnauthorizedRoleException("OWNER nije autorizovan za koriscenje bank-account servisa");
    }

    private BankAccount getOrThrow(String email, String currencyCode) {
        return repository.findByEmailAndCurrencyCode(email, currencyCode.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Korisnik " + email + " nema racun za valutu " + currencyCode.toUpperCase()));
    }

    private BankAccountDto toDto(BankAccount account) {
        return new BankAccountDto(account.getId(), account.getEmail(),
                account.getCurrencyCode(), account.getAmount());
    }
}
