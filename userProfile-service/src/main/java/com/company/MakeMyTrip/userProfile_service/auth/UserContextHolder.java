package com.company.MakeMyTrip.userProfile_service.auth;

public final class UserContextHolder {

    private static final ThreadLocal<Long> CURRENT_USER_ID =
            new ThreadLocal<>();

    private UserContextHolder() {
        // Utility class
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    static void clear() {
        CURRENT_USER_ID.remove();
    }
}