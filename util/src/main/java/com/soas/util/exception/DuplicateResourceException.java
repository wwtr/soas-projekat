package com.soas.util.exception;

/**
 * Pokusaj kreiranja resursa koji vec postoji (npr. drugi OWNER,
 * duplirana email adresa, drugi racun za istu valutu). -> 409
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
