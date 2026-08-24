package com.soas.currencyconversion.controller;

import com.soas.currencyconversion.service.CurrencyConversionService;
import com.soas.servicelibrary.dto.TransactionResponse;
import com.soas.servicelibrary.security.AuthHeaders;
import com.soas.servicelibrary.security.AuthUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/currency-conversion")
public class CurrencyConversionController {

    private final CurrencyConversionService service;

    public CurrencyConversionController(CurrencyConversionService service) {
        this.service = service;
    }

    @GetMapping
    public TransactionResponse convert(@RequestParam("from") String from,
                                       @RequestParam("to") String to,
                                       @RequestParam("quantity") BigDecimal quantity,
                                       @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                       @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String email) {
        return service.convert(from, to, quantity, AuthUtils.resolveRole(role), email);
    }

    // poziva ga trade servis kada mora da predje preko EUR ili USD
    @PostMapping("/internal")
    public TransactionResponse convertForUser(@RequestParam("email") String email,
                                              @RequestParam("from") String from,
                                              @RequestParam("to") String to,
                                              @RequestParam("quantity") BigDecimal quantity) {
        return service.convertForUser(email, from, to, quantity);
    }
}
