package com.company.MakeMyTrip.Auth_service.advices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private Instant timestamp;

    private int status;

    private String error;

    private String message;

    private String path;

    private List<String> subErrors;
}