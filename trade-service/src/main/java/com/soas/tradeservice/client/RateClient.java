package com.soas.tradeservice.client;

import com.soas.servicelibrary.proxy.CryptoExchangeProxy;
import com.soas.servicelibrary.proxy.CurrencyExchangeProxy;
import com.soas.util.exception.ExternalApiException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

// pribavljanje kurseva je jedino mesto gde trade servis zavisi od spoljnog sveta,
// pa su retry i circuit breaker postavljeni bas ovde
@Component
public class RateClient {

    private static final Logger log = LoggerFactory.getLogger(RateClient.class);

    private final CryptoExchangeProxy cryptoExchangeProxy;
    private final CurrencyExchangeProxy currencyExchangeProxy;

    public RateClient(CryptoExchangeProxy cryptoExchangeProxy,
                      CurrencyExchangeProxy currencyExchangeProxy) {
        this.cryptoExchangeProxy = cryptoExchangeProxy;
        this.currencyExchangeProxy = currencyExchangeProxy;
    }

    @Retry(name = "default")
    @CircuitBreaker(name = "cb", fallbackMethod = "cryptoRateFallback")
    public BigDecimal cryptoRate(String from, String to) {
        return cryptoExchangeProxy.getExchangeValue(from, to).getConversionMultiple();
    }

    @Retry(name = "default")
    @CircuitBreaker(name = "cb", fallbackMethod = "fiatRateFallback")
    public BigDecimal fiatRate(String from, String to) {
        return currencyExchangeProxy.getExchangeValue(from, to).getConversionMultiple();
    }

    @Retry(name = "default")
    @CircuitBreaker(name = "cb", fallbackMethod = "isFiatFallback")
    public boolean isFiat(String code) {
        return Boolean.TRUE.equals(currencyExchangeProxy.isFiat(code));
    }

    // fallback metode moraju da imaju iste parametre plus Throwable na kraju

    public BigDecimal cryptoRateFallback(String from, String to, Throwable throwable) {
        log.warn("crypto-exchange nedostupan za {} -> {}: {}", from, to, throwable.getMessage());
        throw new ExternalApiException("Kurs za " + from + " -> " + to
                + " trenutno nije moguce pribaviti, pokusajte kasnije");
    }

    public BigDecimal fiatRateFallback(String from, String to, Throwable throwable) {
        log.warn("currency-exchange nedostupan za {} -> {}: {}", from, to, throwable.getMessage());
        throw new ExternalApiException("Kurs za " + from + " -> " + to
                + " trenutno nije moguce pribaviti, pokusajte kasnije");
    }

    public boolean isFiatFallback(String code, Throwable throwable) {
        log.warn("currency-exchange nedostupan pri proveri valute {}: {}", code, throwable.getMessage());
        throw new ExternalApiException("Nije moguce utvrditi tip valute " + code + ", pokusajte kasnije");
    }
}
