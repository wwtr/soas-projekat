package com.soas.servicelibrary.proxy;

import com.soas.servicelibrary.dto.ExchangeRateApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// eksterni API za kurseve fiat valuta, zato ima apsolutni url a ne naziv iz Eureke
@FeignClient(name = "exchange-rate-api", url = "${external.fiat-api.url:https://open.er-api.com/v6}")
public interface ExchangeRateApiProxy {

    @GetMapping("/latest/{base}")
    ExchangeRateApiResponse getRates(@PathVariable("base") String base);
}
