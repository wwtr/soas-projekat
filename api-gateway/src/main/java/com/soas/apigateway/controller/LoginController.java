package com.soas.apigateway.controller;

import com.soas.servicelibrary.security.AuthHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// front-end-u treba jedna tacka na kojoj proverava kredencijale i saznaje svoju ulogu
@RestController
public class LoginController {

    @GetMapping("/login")
    public Map<String, String> login(@RequestHeader(AuthHeaders.USER_EMAIL) String email,
                                     @RequestHeader(AuthHeaders.USER_ROLE) String role) {
        return Map.of("email", email, "role", role);
    }
}
