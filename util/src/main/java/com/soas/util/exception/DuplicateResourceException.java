package com.soas.util.exception;

// drugi OWNER, duplirana email adresa, drugi racun za istu valutu
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
