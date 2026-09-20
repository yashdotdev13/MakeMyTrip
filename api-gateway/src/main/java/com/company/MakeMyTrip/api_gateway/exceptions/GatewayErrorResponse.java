package com.company.MakeMyTrip.api_gateway.exceptions;

import java.time.Instant;

public record GatewayErrorResponse (


        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId
){
}
