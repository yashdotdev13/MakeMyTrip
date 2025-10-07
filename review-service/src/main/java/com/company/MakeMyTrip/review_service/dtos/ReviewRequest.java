package com.company.MakeMyTrip.review_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequest {

    private Long bookingId;
    private Integer rating;
    private String title;
    private String comment;
}
