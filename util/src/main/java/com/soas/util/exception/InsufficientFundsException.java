package com.soas.util.exception;

// korisnik nema dovoljno sredstava za trazenu razmenu
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
