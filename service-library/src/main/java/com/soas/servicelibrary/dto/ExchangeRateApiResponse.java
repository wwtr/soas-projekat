package com.soas.servicelibrary.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

// odgovor eksternog servisa open.er-api.com
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExchangeRateApiResponse {

    private String result;

    @JsonProperty("base_code")
    private String baseCode;

    private Map<String, BigDecimal> rates;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public Map<String, BigDecimal> getRates() {
        return rates;
    }

    public void setRates(Map<String, BigDecimal> rates) {
        this.rates = rates;
    }
}
