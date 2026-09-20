package com.company.MakeMyTrip.api_gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(
            ServerWebExchange exchange,
            org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        long startTime = System.currentTimeMillis();

        String method = exchange.getRequest()
                .getMethod()
                .name();

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst("X-Correlation-Id");

        return chain.filter(exchange)
                .doFinally(signalType -> {

                    long duration =
                            System.currentTimeMillis() - startTime;

                    HttpStatusCode statusCode =
                            exchange.getResponse().getStatusCode();

                    log.info(
                            "Gateway request completed | method={} path={} status={} durationMs={} correlationId={}",
                            method,
                            path,
                            statusCode != null
                                    ? statusCode.value()
                                    : "UNKNOWN",
                            duration,
                            correlationId
                    );
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}