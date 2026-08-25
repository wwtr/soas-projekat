package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.CurrencyExchangeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "crypto-exchange")
public interface CryptoExchangeProxy {

    @GetMapping("/crypto-exchange/from/{from}/to/{to}")
    CurrencyExchangeDto getExchangeValue(@PathVariable("from") String from,
                                         @PathVariable("to") String to);
}
