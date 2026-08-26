package com.soas.apigateway.filter;

import com.soas.servicelibrary.dto.UserDto;
import com.soas.servicelibrary.security.AuthHeaders;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Set;

@Component
public class AuthenticationFilter implements WebFilter, Ordered {

    // kursevi su javni podatak, ostalo trazi prijavu
    private static final Set<String> PUBLIC_PREFIXES = Set.of("/currency-exchange", "/crypto-exchange");

    private final RestTemplate restTemplate;

    public AuthenticationFilter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // interne rute sluze samo za komunikaciju medju servisima i ne izlaze napolje
        if (path.contains("/internal") || path.startsWith("/users/authenticate")) {
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);
            return exchange.getResponse().setComplete();
        }

        if (isPublic(path)) {
            return chain.filter(stripAuthHeaders(exchange));
        }

        List<String> authorization = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION);
        if (authorization == null || authorization.isEmpty() || !authorization.get(0).startsWith("Basic ")) {
            return unauthorized(exchange);
        }

        String[] credentials;
        try {
            String decoded = new String(Base64.getDecoder()
                    .decode(authorization.get(0).substring(6).trim()), StandardCharsets.UTF_8);
            credentials = decoded.split(":", 2);
        } catch (IllegalArgumentException ex) {
            return unauthorized(exchange);
        }

        if (credentials.length != 2) {
            return unauthorized(exchange);
        }

        String email = credentials[0];
        String password = credentials[1];

        return Mono.fromCallable(() -> restTemplate.getForObject(
                        "http://users-service/users/authenticate?email={email}&password={password}",
                        UserDto.class, email, password))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(user -> {
                    if (user == null || user.getRole() == null) {
                        return unauthorized(exchange);
                    }
                    // set a ne add, da klijent ne bi mogao da podmetne svoju ulogu
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .headers(headers -> {
                                headers.set(AuthHeaders.USER_EMAIL, user.getEmail());
                                headers.set(AuthHeaders.USER_ROLE, user.getRole().name());
                            })
                            .build();
                    return chain.filter(exchange.mutate().request(request).build());
                })
                .onErrorResume(ex -> unauthorized(exchange));
    }

    private ServerWebExchange stripAuthHeaders(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(AuthHeaders.USER_EMAIL);
                    headers.remove(AuthHeaders.USER_ROLE);
                })
                .build();
        return exchange.mutate().request(request).build();
    }

    private boolean isPublic(String path) {
        return PUBLIC_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"SOAS\"");
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
