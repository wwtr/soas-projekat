package com.soas.usersservice.service;

import com.soas.servicelibrary.dto.UserDto;
import com.soas.servicelibrary.enums.Role;
import com.soas.servicelibrary.proxy.BankAccountProxy;
import com.soas.servicelibrary.proxy.CryptoWalletProxy;
import com.soas.usersservice.entity.User;
import com.soas.usersservice.repository.UserRepository;
import com.soas.util.exception.DuplicateResourceException;
import com.soas.util.exception.InvalidRequestException;
import com.soas.util.exception.ResourceNotFoundException;
import com.soas.util.exception.UnauthorizedRoleException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository repository;
    private final BankAccountProxy bankAccountProxy;
    private final CryptoWalletProxy cryptoWalletProxy;

    public UserService(UserRepository repository,
                       BankAccountProxy bankAccountProxy,
                       CryptoWalletProxy cryptoWalletProxy) {
        this.repository = repository;
        this.bankAccountProxy = bankAccountProxy;
        this.cryptoWalletProxy = cryptoWalletProxy;
    }

    public List<UserDto> findAll(Role caller) {
        requireAdminOrOwner(caller);
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public UserDto findById(Long id, Role caller) {
        requireAdminOrOwner(caller);
        return toDto(getOrThrow(id));
    }

    // koristi ga API Gateway prilikom basic autentikacije, zato nema provere uloge
    public UserDto authenticate(String email, String password) {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa email adresom " + email + " ne postoji"));
        if (!user.getPassword().equals(password)) {
            throw new UnauthorizedRoleException("Pogresna lozinka za korisnika " + email);
        }
        return toDto(user);
    }

    @Transactional
    public UserDto create(UserDto dto, Role caller) {
        requireAdminOrOwner(caller);
        validatePayload(dto);

        if (caller == Role.ADMIN && dto.getRole() != Role.USER) {
            throw new UnauthorizedRoleException("ADMIN moze da dodaje samo korisnike sa ulogom USER");
        }
        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Korisnik sa email adresom " + dto.getEmail() + " vec postoji");
        }
        if (dto.getRole() == Role.OWNER && repository.existsByRole(Role.OWNER)) {
            throw new DuplicateResourceException("U sistemu vec postoji korisnik sa ulogom OWNER");
        }

        User saved = repository.save(new User(dto.getEmail(), dto.getPassword(), dto.getRole()));

        if (saved.getRole() == Role.USER) {
            openAccounts(saved.getEmail());
        }
        return toDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto, Role caller) {
        requireAdminOrOwner(caller);
        User user = getOrThrow(id);

        if (caller == Role.ADMIN && user.getRole() != Role.USER) {
            throw new UnauthorizedRoleException("ADMIN moze da azurira samo korisnike sa ulogom USER");
        }
        if (caller == Role.ADMIN && dto.getRole() != null && dto.getRole() != Role.USER) {
            throw new UnauthorizedRoleException("ADMIN ne moze da menja ulogu korisnika");
        }
        // email je vezan za bankovni racun i novcanik, pa se ne menja naknadno
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
            throw new InvalidRequestException("Email adresa postojeceg korisnika ne moze da se menja");
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(dto.getPassword());
        }

        if (dto.getRole() != null && dto.getRole() != user.getRole()) {
            if (dto.getRole() == Role.OWNER && repository.existsByRole(Role.OWNER)) {
                throw new DuplicateResourceException("U sistemu vec postoji korisnik sa ulogom OWNER");
            }
            Role previous = user.getRole();
            user.setRole(dto.getRole());

            // racun i novcanik prate ulogu USER
            if (previous == Role.USER) {
                closeAccounts(user.getEmail());
            } else if (dto.getRole() == Role.USER) {
                openAccounts(user.getEmail());
            }
        }

        return toDto(repository.save(user));
    }

    @Transactional
    public void delete(Long id, Role caller) {
        if (caller != Role.OWNER) {
            throw new UnauthorizedRoleException("Samo OWNER moze da brise korisnike");
        }
        User user = getOrThrow(id);
        if (user.getRole() == Role.USER) {
            closeAccounts(user.getEmail());
        }
        repository.delete(user);
    }

    private void openAccounts(String email) {
        bankAccountProxy.createStartingAccount(email);
        cryptoWalletProxy.createStartingWallet(email);
    }

    private void closeAccounts(String email) {
        bankAccountProxy.deleteAccountsForUser(email);
        cryptoWalletProxy.deleteWalletsForUser(email);
    }

    private void requireAdminOrOwner(Role caller) {
        if (caller != Role.OWNER && caller != Role.ADMIN) {
            throw new UnauthorizedRoleException("Korisnik sa ulogom USER nema pristup users servisu");
        }
    }

    private void validatePayload(UserDto dto) {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new InvalidRequestException("Email adresa je obavezna");
        }
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new InvalidRequestException("Lozinka je obavezna");
        }
        if (dto.getRole() == null) {
            throw new InvalidRequestException("Uloga je obavezna (OWNER, ADMIN ili USER)");
        }
    }

    private User getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Korisnik sa id " + id + " ne postoji"));
    }

    // lozinka se ne vraca u odgovoru, kredencijali stoje u README fajlu
    private UserDto toDto(User user) {
        return new UserDto(user.getId(), user.getEmail(), null, user.getRole());
    }
}
