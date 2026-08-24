package com.soas.cryptowallet.controller;

import com.soas.cryptowallet.service.CryptoWalletService;
import com.soas.servicelibrary.dto.CryptoWalletDto;
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
@RequestMapping("/crypto-wallet")
public class CryptoWalletController {

    private final CryptoWalletService service;

    public CryptoWalletController(CryptoWalletService service) {
        this.service = service;
    }

    @GetMapping
    public List<CryptoWalletDto> getAll(@RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.findAll(AuthUtils.resolveRole(role));
    }

    @GetMapping("/user/{email}")
    public List<CryptoWalletDto> getForUser(@PathVariable("email") String email,
                                           @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                           @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String callerEmail) {
        return service.findByEmail(email, AuthUtils.resolveRole(role), callerEmail);
    }

    @GetMapping("/user/{email}/{cryptoCode}")
    public CryptoWalletDto getOne(@PathVariable("email") String email,
                                 @PathVariable("cryptoCode") String cryptoCode,
                                 @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                 @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String callerEmail) {
        return service.findOne(email, cryptoCode, AuthUtils.resolveRole(role), callerEmail);
    }

    @PostMapping
    public ResponseEntity<CryptoWalletDto> create(@RequestBody CryptoWalletDto dto,
                                                 @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto, AuthUtils.resolveRole(role)));
    }

    @PutMapping("/{id}")
    public CryptoWalletDto update(@PathVariable("id") Long id,
                                 @RequestBody CryptoWalletDto dto,
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
    public List<CryptoWalletDto> getForUserInternal(@PathVariable("email") String email) {
        return service.findAllForUser(email);
    }

    @PostMapping("/internal/{email}")
    public CryptoWalletDto createStartingWallet(@PathVariable("email") String email) {
        return service.createStartingWallet(email);
    }

    @DeleteMapping("/internal/{email}")
    public ResponseEntity<Void> deleteForUser(@PathVariable("email") String email) {
        service.deleteWalletsForUser(email);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/{email}/{cryptoCode}")
    public CryptoWalletDto changeAmount(@PathVariable("email") String email,
                                       @PathVariable("cryptoCode") String cryptoCode,
                                       @RequestParam("delta") BigDecimal delta) {
        return service.changeAmount(email, cryptoCode, delta);
    }
}
