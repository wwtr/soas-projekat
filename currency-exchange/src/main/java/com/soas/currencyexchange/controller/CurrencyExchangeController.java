package com.soas.currencyexchange.controller;

import com.soas.currencyexchange.service.CurrencyExchangeService;
import com.soas.servicelibrary.dto.CurrencyExchangeDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// servisu sme da pristupi korisnik sa bilo kojom ulogom, pa nema provere header-a
@RestController
@RequestMapping("/currency-exchange")
public class CurrencyExchangeController {

    private final CurrencyExchangeService service;

    public CurrencyExchangeController(CurrencyExchangeService service) {
        this.service = service;
    }

    @GetMapping("/from/{from}/to/{to}")
    public CurrencyExchangeDto getExchangeValue(@PathVariable("from") String from,
                                                @PathVariable("to") String to) {
        return service.getExchangeValue(from, to);
    }
}
