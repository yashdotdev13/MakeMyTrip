package com.company.MakeMyTrip.api_gateway.filters;

import com.company.MakeMyTrip.api_gateway.JwtService;
import com.company.MakeMyTrip.api_gateway.config.GatewaySecurityProperties;
import com.company.MakeMyTrip.api_gateway.exceptions.GatewayErrorWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import static org.mockito.Mockito.when;

class AuthenticationFilterTest {

    private JwtService jwtService;
    private GatewayErrorWriter gatewayErrorWriter;
    private GatewaySecurityProperties securityProperties;
    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    void setUp() {

        jwtService = Mockito.mock(JwtService.class);
        gatewayErrorWriter = Mockito.mock(GatewayErrorWriter.class);
        securityProperties = new GatewaySecurityProperties();

        securityProperties.setPublicPaths(
                List.of(
                        "/auth/login",
                        "/auth/register"
                )
        );

        authenticationFilter = new AuthenticationFilter(
                jwtService,
                gatewayErrorWriter,
                securityProperties
        );
    }

    @Test
    void shouldAllowPublicEndpointWithoutAuthentication() {

        ServerWebExchange exchange = exchange(
                "/auth/login",
                null
        );

        var chain = Mockito.mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
        );

        when(chain.filter(exchange))
                .thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(exchange, chain)
                .block();

        Mockito.verify(chain).filter(exchange);
        Mockito.verifyNoInteractions(jwtService);
    }

    @Test
    void shouldRejectRequestWhenAuthorizationHeaderIsMissing() {

        ServerWebExchange exchange = exchange(
                "/booking/test",
                null
        );

        when(gatewayErrorWriter.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        )).thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(
                        exchange,
                        Mockito.mock(
                                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
                        )
                )
                .block();

        Mockito.verify(gatewayErrorWriter).write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication required"
        );
    }

    @Test
    void shouldRejectEmptyBearerToken() {

        ServerWebExchange exchange = exchange(
                "/booking/test",
                "Bearer "
        );

        when(gatewayErrorWriter.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication token is empty"
        )).thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(
                        exchange,
                        Mockito.mock(
                                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
                        )
                )
                .block();

        Mockito.verify(gatewayErrorWriter).write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Authentication token is empty"
        );
    }

    @Test
    void shouldRejectInvalidJwt() {

        String token = "invalid-token";

        ServerWebExchange exchange = exchange(
                "/booking/test",
                "Bearer " + token
        );

        when(jwtService.getUserIdFromToken(token))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        when(gatewayErrorWriter.write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Invalid or expired authentication token"
        )).thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(
                        exchange,
                        Mockito.mock(
                                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
                        )
                )
                .block();

        Mockito.verify(gatewayErrorWriter).write(
                exchange,
                HttpStatus.UNAUTHORIZED,
                "Invalid or expired authentication token"
        );
    }

    @Test
    void shouldAuthenticateValidJwt() {

        String token = "valid-token";
        String userId = "user-123";

        ServerWebExchange exchange = exchange(
                "/booking/test",
                "Bearer " + token
        );

        when(jwtService.getUserIdFromToken(token))
                .thenReturn(userId);

        var chain = Mockito.mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
        );

        when(chain.filter(Mockito.any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(exchange, chain)
                .block();

        Mockito.verify(chain).filter(
                Mockito.argThat(modifiedExchange ->
                        userId.equals(
                                modifiedExchange.getRequest()
                                        .getHeaders()
                                        .getFirst("X-User-Id")
                        )
                )
        );
    }

    @Test
    void shouldOverwriteClientSuppliedUserId() {

        String token = "valid-token";
        String authenticatedUserId = "user-from-jwt";

        MockServerHttpRequest request =
                MockServerHttpRequest
                        .get("/booking/test")
                        .header("Authorization", "Bearer " + token)
                        .header("X-User-Id", "malicious-user")
                        .build();

        ServerWebExchange exchange =
                MockServerWebExchange.from(request);

        when(jwtService.getUserIdFromToken(token))
                .thenReturn(authenticatedUserId);

        var chain = Mockito.mock(
                org.springframework.cloud.gateway.filter.GatewayFilterChain.class
        );

        when(chain.filter(Mockito.any(ServerWebExchange.class)))
                .thenReturn(Mono.empty());

        authenticationFilter
                .apply(new AuthenticationFilter.Config())
                .filter(exchange, chain)
                .block();

        Mockito.verify(chain).filter(
                Mockito.argThat(modifiedExchange ->
                        authenticatedUserId.equals(
                                modifiedExchange.getRequest()
                                        .getHeaders()
                                        .getFirst("X-User-Id")
                        )
                )
        );
    }

    private ServerWebExchange exchange(
            String path,
            String authorizationHeader) {

        MockServerHttpRequest.BaseBuilder<?> builder =
                MockServerHttpRequest.get(path);

        if (authorizationHeader != null) {
            builder.header(
                    "Authorization",
                    authorizationHeader
            );
        }

        return MockServerWebExchange.from(
                builder.build()
        );
    }
}