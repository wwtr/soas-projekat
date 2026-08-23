package com.soas.servicelibrary.dto;

import java.math.BigDecimal;

/**
 * Jedan red crypto novcanika: koliko odredjene crypto valute korisnik poseduje.
 */
public class CryptoWalletDto {

    private Long id;
    private String email;
    private String cryptoCode;
    private BigDecimal amount;

    public CryptoWalletDto() {
    }

    public CryptoWalletDto(Long id, String email, String cryptoCode, BigDecimal amount) {
        this.id = id;
        this.email = email;
        this.cryptoCode = cryptoCode;
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

    public String getCryptoCode() {
        return cryptoCode;
    }

    public void setCryptoCode(String cryptoCode) {
        this.cryptoCode = cryptoCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
