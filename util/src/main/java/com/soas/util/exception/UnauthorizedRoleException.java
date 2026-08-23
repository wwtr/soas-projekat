package com.soas.util.exception;

/**
 * Korisnik je autentikovan ali njegova uloga nema pravo na ovu operaciju. -> 403
 */
public class UnauthorizedRoleException extends RuntimeException {

    public UnauthorizedRoleException(String message) {
        super(message);
    }
}
