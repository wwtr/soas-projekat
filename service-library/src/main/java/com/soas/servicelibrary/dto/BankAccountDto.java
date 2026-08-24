package com.soas.servicelibrary.dto;

import java.math.BigDecimal;

public class BankAccountDto {

    private Long id;
    private String email;
    private String currencyCode;
    private BigDecimal amount;

    public BankAccountDto() {
    }

    public BankAccountDto(Long id, String email, String currencyCode, BigDecimal amount) {
        this.id = id;
        this.email = email;
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
