package com.company.MakeMyTrip.review_service.advices;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {

    private LocalDateTime timeStamp;
    private String message;
    private T data;
    private ApiError error;

    // ✅ Success response constructor
    public ApiResponse(String message, T data) {
        this.timeStamp = LocalDateTime.now();
        this.message = message;
        this.data = data;
    }

    // ✅ Error response constructor
    public ApiResponse(ApiError error) {
        this.timeStamp = LocalDateTime.now();
        this.error = error;
        this.message = error.getMessage();
    }

    // ✅ Optional: success message only (no data)
    public ApiResponse(String message) {
        this.timeStamp = LocalDateTime.now();
        this.message = message;
    }
}
