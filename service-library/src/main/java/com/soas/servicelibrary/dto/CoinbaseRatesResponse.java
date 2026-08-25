package com.soas.servicelibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.Map;

// odgovor eksternog servisa api.coinbase.com, kursevi su ugnjezdeni u "data"
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoinbaseRatesResponse {

    private RateData data;

    public RateData getData() {
        return data;
    }

    public void setData(RateData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RateData {

        private String currency;
        private Map<String, BigDecimal> rates;

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public Map<String, BigDecimal> getRates() {
            return rates;
        }

        public void setRates(Map<String, BigDecimal> rates) {
            this.rates = rates;
        }
    }
}
