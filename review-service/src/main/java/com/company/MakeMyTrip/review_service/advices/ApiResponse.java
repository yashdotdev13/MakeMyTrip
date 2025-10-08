package com.company.MakeMyTrip.review_service.advices;


import com.company.MakeMyTrip.review_service.dtos.ReviewResponse;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {

    private LocalDateTime timeStamp;
    private T data;
    private ApiError error;

    public ApiResponse(String reviewCreatedSuccessfully, @NotNull ReviewResponse response){
        this.timeStamp = LocalDateTime.now();
    }

    public ApiResponse(T data){
        this("Review created successfully", response);
        this.data = data;
    }

    public ApiResponse(ApiError error){
        this("Review created successfully", response);
        this.error = error;
    }
}
