package com.company.MakeMyTrip.review_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewSummaryResponse extends ReviewResponse {

    private Long bookingId;
    private Double averageRating;
    private Double totalReviews;
}
