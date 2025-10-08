package com.company.MakeMyTrip.review_service.advices;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    private String message;
    private String details;
}
