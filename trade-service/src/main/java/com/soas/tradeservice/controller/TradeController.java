package com.soas.tradeservice.controller;

import com.soas.servicelibrary.dto.TransactionResponse;
import com.soas.servicelibrary.security.AuthHeaders;
import com.soas.servicelibrary.security.AuthUtils;
import com.soas.tradeservice.service.TradeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/trade-service")
public class TradeController {

    private final TradeService service;

    public TradeController(TradeService service) {
        this.service = service;
    }

    @GetMapping
    public TransactionResponse trade(@RequestParam("from") String from,
                                     @RequestParam("to") String to,
                                     @RequestParam("quantity") BigDecimal quantity,
                                     @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role,
                                     @RequestHeader(value = AuthHeaders.USER_EMAIL, required = false) String email) {
        return service.trade(from, to, quantity, AuthUtils.resolveRole(role), email);
    }
}
