package com.company.MakeMyTrip.booking_service.advices;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {

    private HttpStatus status;
    private String message;
    private List<String> subErrors;

    public ApiError(String message) {
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public ApiError(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
