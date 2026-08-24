package com.soas.currencyexchange.service;

import com.soas.servicelibrary.dto.CurrencyExchangeDto;
import com.soas.servicelibrary.dto.ExchangeRateApiResponse;
import com.soas.servicelibrary.proxy.ExchangeRateApiProxy;
import com.soas.util.exception.ExternalApiException;
import com.soas.util.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyExchangeService {

    private final ExchangeRateApiProxy apiProxy;

    @Value("${server.port}")
    private String port;

    public CurrencyExchangeService(ExchangeRateApiProxy apiProxy) {
        this.apiProxy = apiProxy;
    }

    public CurrencyExchangeDto getExchangeValue(String from, String to) {
        String source = normalize(from);
        String target = normalize(to);

        if (source.equals(target)) {
            return new CurrencyExchangeDto(source, target, BigDecimal.ONE, port);
        }

        ExchangeRateApiResponse response;
        try {
            response = apiProxy.getRates(source);
        } catch (Exception ex) {
            throw new ExternalApiException("Servis za kurseve fiat valuta trenutno nije dostupan");
        }

        if (response == null || !"success".equalsIgnoreCase(response.getResult())) {
            throw new InvalidRequestException("Valuta " + source + " nije podrzana");
        }

        Map<String, BigDecimal> rates = response.getRates();
        BigDecimal rate = rates == null ? null : rates.get(target);
        if (rate == null) {
            throw new InvalidRequestException("Valuta " + target + " nije podrzana");
        }

        return new CurrencyExchangeDto(source, target, rate, port);
    }

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidRequestException("Kod valute je obavezan");
        }
        return code.trim().toUpperCase();
    }
}
