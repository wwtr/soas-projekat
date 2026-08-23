package com.soas.util.exception;

/**
 * Eksterni API za kurseve nije dostupan ili je vratio neocekivan odgovor. -> 503
 */
public class ExternalApiException extends RuntimeException {

    public ExternalApiException(String message) {
        super(message);
    }
}
