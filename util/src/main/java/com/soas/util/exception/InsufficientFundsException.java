package com.soas.util.exception;

/**
 * Korisnik nema dovoljno sredstava za trazenu razmenu. -> 400
 */
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
