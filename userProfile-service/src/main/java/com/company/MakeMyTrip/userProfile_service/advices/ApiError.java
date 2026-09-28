package com.company.MakeMyTrip.userProfile_service.advices;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ApiError {

    private final String status;
    private final String message;
    private final List<String> subErrors;
}