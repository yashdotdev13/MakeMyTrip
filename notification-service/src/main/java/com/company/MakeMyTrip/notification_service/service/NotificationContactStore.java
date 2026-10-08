package com.company.MakeMyTrip.notification_service.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationContactStore {

    private final Map<Long, String> emailByUserId = new ConcurrentHashMap<>();

    public void saveEmail(Long userId, String email) {
        emailByUserId.put(userId, email);
    }

    public String getEmail(Long userId) {
        return emailByUserId.get(userId);
    }
}