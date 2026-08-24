package com.soas.bankaccount.controller;

import com.soas.bankaccount.service.BankAccountService;
import com.soas.servicelibrary.dto.BankAccountDto;
import com.soas.servicelibrary.security.AuthHeaders;
import com.soas.servicelibrary.security.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/bank-account")
public class BankAccountController {

    private final BankAccountService service;

    public BankAccountController(BankAccountService service) {
        this.service = service;
    }

    @GetMapping
    public List<BankAccountDto> getAll(@RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.findAll(AuthUtils.resolveRole(role));
    }

    @GetMapping("/user/{email}")
    public List<BankAccountDto> getForUser(@PathVariable("email") String email,
                                           @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                           @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String callerEmail) {
        return service.findByEmail(email, AuthUtils.resolveRole(role), callerEmail);
    }

    @GetMapping("/user/{email}/{currencyCode}")
    public BankAccountDto getOne(@PathVariable("email") String email,
                                 @PathVariable("currencyCode") String currencyCode,
                                 @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                 @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String callerEmail) {
        return service.findOne(email, currencyCode, AuthUtils.resolveRole(role), callerEmail);
    }

    @PostMapping
    public ResponseEntity<BankAccountDto> create(@RequestBody BankAccountDto dto,
                                                 @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, AuthUtils.resolveRole(role)));
    }

    @PutMapping("/{id}")
    public BankAccountDto update(@PathVariable("id") Long id,
                                 @RequestBody BankAccountDto dto,
                                 @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.update(id, dto, AuthUtils.resolveRole(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        service.delete(id, AuthUtils.resolveRole(role));
        return ResponseEntity.noContent().build();
    }

    // endpointi ispod poziva users-service odnosno servisi za razmenu, ne korisnik direktno

    @GetMapping("/internal/{email}")
    public List<BankAccountDto> getForUserInternal(@PathVariable("email") String email) {
        return service.findAllForUser(email);
    }

    @PostMapping("/internal/{email}")
    public BankAccountDto createStartingAccount(@PathVariable("email") String email) {
        return service.createStartingAccount(email);
    }

    @DeleteMapping("/internal/{email}")
    public ResponseEntity<Void> deleteForUser(@PathVariable("email") String email) {
        service.deleteAccountsForUser(email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/{email}/{currencyCode}")
    public BankAccountDto changeAmount(@PathVariable("email") String email,
                                       @PathVariable("currencyCode") String currencyCode,
                                       @RequestParam("delta") BigDecimal delta) {
        return service.changeAmount(email, currencyCode, delta);
    }
}
