package com.company.MakeMyTrip.api_gateway.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Slf4j
@Component
@Order(-2)
public class GatewayGlobalExceptionHandler implements ErrorWebExceptionHandler {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> handle(
            ServerWebExchange exchange,
            Throwable ex) {

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        log.error(
                "Gateway request failed: {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath(),
                ex
        );

        String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID_HEADER);

        String path = exchange.getRequest()
                .getURI()
                .getPath();

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        GatewayErrorResponse errorResponse =
                new GatewayErrorResponse(
                        Instant.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        "Gateway request failed",
                        path,
                        correlationId
                );

        String responseBody = String.format(
                """
                {
                  "timestamp": "%s",
                  "status": %d,
                  "error": "%s",
                  "message": "%s",
                  "path": "%s",
                  "correlationId": "%s"
                }
                """,
                errorResponse.timestamp(),
                errorResponse.status(),
                errorResponse.error(),
                errorResponse.message(),
                errorResponse.path(),
                errorResponse.correlationId()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse()
                .getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

        return exchange.getResponse()
                .writeWith(
                        Mono.just(
                                exchange.getResponse()
                                        .bufferFactory()
                                        .wrap(bytes)
                        )
                );
    }
}