package com.soas.servicelibrary.security;

import com.soas.servicelibrary.enums.Role;
import com.soas.util.exception.UnauthorizedRoleException;

public final class AuthUtils {

    private AuthUtils() {
    }

    public static Role resolveRole(String header) {
        if (header == null || header.isBlank()) {
            throw new UnauthorizedRoleException("Nedostaje header " + AuthHeaders.USER_ROLE);
        }
        try {
            return Role.valueOf(header.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new UnauthorizedRoleException("Nepoznata uloga: " + header);
        }
    }
}
