package com.soas.servicelibrary.security;

// API Gateway proverava kredencijale i prosledjuje ova dva header-a nizvodnim servisima
public final class AuthHeaders {

    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLE = "X-User-Role";

    private AuthHeaders() {
    }
}
