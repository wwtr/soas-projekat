package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.TransactionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "currency-conversion")
public interface CurrencyConversionProxy {

    // interna varijanta koju koristi trade servis kad mora da predje preko EUR ili USD
    @PostMapping("/currency-conversion/internal")
    TransactionResponse convertForUser(@RequestParam("email") String email,
                                       @RequestParam("from") String from,
                                       @RequestParam("to") String to,
                                       @RequestParam("quantity") BigDecimal quantity);
}
