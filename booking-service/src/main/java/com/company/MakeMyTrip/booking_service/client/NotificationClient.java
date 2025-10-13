package com.company.MakeMyTrip.booking_service.client;

import com.company.MakeMyTrip.booking_service.dtos.EmailRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {


    @PostMapping("/notifications/send-email")
    void sendEmail(@RequestBody EmailRequest request);
}
