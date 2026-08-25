package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.CoinbaseRatesResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// eksterni API za kurseve crypto valuta
@FeignClient(name = "coinbase-api", url = "${external.crypto-api.url:https://api.coinbase.com/v2}")
public interface CryptoRateApiProxy {

    @GetMapping("/exchange-rates")
    CoinbaseRatesResponse getRates(@RequestParam("currency") String currency);
}
