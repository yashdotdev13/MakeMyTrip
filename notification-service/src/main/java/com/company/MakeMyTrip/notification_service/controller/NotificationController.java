package com.company.MakeMyTrip.notification_service.controller;


import com.company.MakeMyTrip.notification_service.dtos.EmailRequest;
import com.company.MakeMyTrip.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-email")
    public String sendEmail(@RequestBody EmailRequest request){
        notificationService.sendEmail(request);
        return "Email sent successfully to: "+request.getTo();
    }
}
