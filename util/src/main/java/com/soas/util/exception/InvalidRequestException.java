package com.soas.util.exception;

/**
 * Zahtev je sintaksno ispravan ali poslovno neispravan
 * (nepostojeca valuta, negativna kolicina, ista polazna i ciljna valuta). -> 400
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
