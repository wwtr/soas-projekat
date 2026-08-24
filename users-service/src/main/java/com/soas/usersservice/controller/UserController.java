package com.soas.usersservice.controller;

import com.soas.servicelibrary.dto.UserDto;
import com.soas.servicelibrary.security.AuthHeaders;
import com.soas.servicelibrary.security.AuthUtils;
import com.soas.usersservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping
    public List<UserDto> getAll(@RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.findAll(AuthUtils.resolveRole(role));
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable("id") Long id,
                           @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.findById(id, AuthUtils.resolveRole(role));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody UserDto dto,
                                          @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        UserDto created = service.create(dto, AuthUtils.resolveRole(role));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable("id") Long id,
                          @RequestBody UserDto dto,
                          @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        return service.update(id, dto, AuthUtils.resolveRole(role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id,
                                       @RequestHeader(value = AuthHeaders.USER_ROLE, required = false) String role) {
        service.delete(id, AuthUtils.resolveRole(role));
        return ResponseEntity.noContent().build();
    }

    // poziva ga iskljucivo API Gateway da bi proverio kredencijale
    @GetMapping("/authenticate")
    public UserDto authenticate(@RequestParam("email") String email,
                                @RequestParam("password") String password) {
        return service.authenticate(email, password);
    }

}
