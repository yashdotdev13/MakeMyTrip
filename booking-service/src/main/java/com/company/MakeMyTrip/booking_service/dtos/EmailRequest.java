package com.company.MakeMyTrip.booking_service.dtos;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    private String to;
    private String subject;
    private String body;
}
