package com.soas.servicelibrary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// zajednicki odgovor za currency-conversion i trade servis:
// stanje racuna ili novcanika posle razmene, plus izvestaj o transakciji
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponse {

    private List<BankAccountDto> bankAccount;
    private List<CryptoWalletDto> cryptoWallet;
    private String message;

    public TransactionResponse() {
    }

    public TransactionResponse(List<BankAccountDto> bankAccount, List<CryptoWalletDto> cryptoWallet, String message) {
        this.bankAccount = bankAccount;
        this.cryptoWallet = cryptoWallet;
        this.message = message;
    }

    public List<BankAccountDto> getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(List<BankAccountDto> bankAccount) {
        this.bankAccount = bankAccount;
    }

    public List<CryptoWalletDto> getCryptoWallet() {
        return cryptoWallet;
    }

    public void setCryptoWallet(List<CryptoWalletDto> cryptoWallet) {
        this.cryptoWallet = cryptoWallet;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
