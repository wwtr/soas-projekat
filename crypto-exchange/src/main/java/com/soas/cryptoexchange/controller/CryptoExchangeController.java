package com.soas.cryptoexchange.controller;

import com.soas.cryptoexchange.service.CryptoExchangeService;
import com.soas.servicelibrary.dto.CurrencyExchangeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// servisu sme da pristupi korisnik sa bilo kojom ulogom
@RestController
@RequestMapping("/crypto-exchange")
public class CryptoExchangeController {

    private final CryptoExchangeService service;

    public CryptoExchangeController(CryptoExchangeService service) {
        this.service = service;
    }

    @GetMapping("/from/{from}/to/{to}")
    public CurrencyExchangeDto getExchangeValue(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        return service.getExchangeValue(from, to);
    }
}
