package com.soas.util.exception;

/**
 * Trazeni resurs (korisnik, racun, novcanik) ne postoji. -> 404
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
