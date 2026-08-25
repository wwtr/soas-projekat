package com.soas.cryptoexchange.service;

import com.soas.servicelibrary.dto.CoinbaseRatesResponse;
import com.soas.servicelibrary.dto.CurrencyExchangeDto;
import com.soas.servicelibrary.proxy.CryptoRateApiProxy;
import com.soas.util.exception.ExternalApiException;
import com.soas.util.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CryptoExchangeService {

    private final CryptoRateApiProxy apiProxy;

    @Value("${server.port}")
    private String port;

    public CryptoExchangeService(CryptoRateApiProxy apiProxy) {
        this.apiProxy = apiProxy;
    }

    public CurrencyExchangeDto getExchangeValue(String from, String to) {
        String source = normalize(from);
        String target = normalize(to);

        if (source.equals(target)) {
            return new CurrencyExchangeDto(source, target, BigDecimal.ONE, port);
        }

        CoinbaseRatesResponse response;
        try {
            response = apiProxy.getRates(source);
        } catch (Exception ex) {
            throw new ExternalApiException("Servis za kurseve crypto valuta trenutno nije dostupan");
        }

        if (response == null || response.getData() == null || response.getData().getRates() == null) {
            throw new InvalidRequestException("Valuta " + source + " nije podrzana");
        }

        Map<String, BigDecimal> rates = response.getData().getRates();
        BigDecimal rate = rates.get(target);
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
