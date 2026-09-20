package com.company.MakeMyTrip.api_gateway.filters;

import com.company.MakeMyTrip.api_gateway.JwtService;
import com.company.MakeMyTrip.api_gateway.exceptions.GatewayErrorWriter;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Slf4j
@Component
public class AuthenticationFilter
        extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_HEADER = "X-User-Id";

    private final JwtService jwtService;
    private final GatewayErrorWriter gatewayErrorWriter;

    public AuthenticationFilter(
            JwtService jwtService,
            GatewayErrorWriter gatewayErrorWriter) {

        super(Config.class);
        this.jwtService = jwtService;
        this.gatewayErrorWriter = gatewayErrorWriter;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            String path = exchange.getRequest()
                    .getURI()
                    .getPath();

            if (path.equals("/auth/login")
                    || path.equals("/auth/register")) {

                log.debug(
                        "Public endpoint, skipping authentication: {}",
                        path
                );

                return chain.filter(exchange);
            }

            log.debug("Authenticating request: {}", path);

            String authorizationHeader =
                    exchange.getRequest()
                            .getHeaders()
                            .getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null
                    || !authorizationHeader.startsWith(BEARER_PREFIX)) {

                log.warn(
                        "Missing or invalid Authorization header: {}",
                        path
                );

                return gatewayErrorWriter.write(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Authentication required"
                );
            }

            String token = authorizationHeader
                    .substring(BEARER_PREFIX.length())
                    .trim();

            if (token.isEmpty()) {

                log.warn("Empty JWT token: {}", path);

                return gatewayErrorWriter.write(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Authentication token is empty"
                );
            }

            try {

                String userId = jwtService.getUserIdFromToken(token);

                ServerWebExchange modifiedExchange = exchange
                        .mutate()
                        .request(request -> request
                                .headers(headers ->
                                        headers.remove(USER_ID_HEADER))
                                .header(USER_ID_HEADER, userId))
                        .build();

                log.debug(
                        "Request authenticated successfully: {}",
                        path
                );

                return chain.filter(modifiedExchange);

            } catch (JwtException | IllegalArgumentException e) {

                log.warn(
                        "JWT authentication failed: {}",
                        path
                );

                return gatewayErrorWriter.write(
                        exchange,
                        HttpStatus.UNAUTHORIZED,
                        "Invalid or expired authentication token"
                );
            }
        };
    }

    public static class Config {
        // Reserved for future filter configuration
    }
}