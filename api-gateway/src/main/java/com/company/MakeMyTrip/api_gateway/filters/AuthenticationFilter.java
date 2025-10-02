package com.company.MakeMyTrip.api_gateway.filters;




import com.company.MakeMyTrip.api_gateway.JwtService;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;


@Slf4j
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AbstractGatewayFilterFactory.NameConfig> {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService){
        super(NameConfig.class);
        this.jwtService = jwtService;
    }

    @Override
    public GatewayFilter apply(NameConfig config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();


            if (path.equals("/auth/login") || path.equals("/auth/register")) {
                log.info("Public path, skipping authentication: {}", path);
                return chain.filter(exchange);
            }

            log.info("Authenticating request to: {}", path);

            final String tokenHeader = exchange.getRequest().getHeaders().getFirst("Authorization");

            if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                log.error("Authorization token header not found");
                return exchange.getResponse().setComplete();
            }

            final String token = tokenHeader.substring(7).trim();

            try {
                String userId = jwtService.getUserIdFromToken(token);
                ServerWebExchange modifiedExchange = exchange
                        .mutate()
                        .request(r -> r.header("X-User-Id", userId))
                        .build();

                log.info("Authenticated user ID: {}", userId);
                return chain.filter(modifiedExchange);
            } catch (JwtException e) {
                log.error("Jwt Exception: {}", e.getLocalizedMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }


    public static class Config {
        // You can leave this empty
    }
}
