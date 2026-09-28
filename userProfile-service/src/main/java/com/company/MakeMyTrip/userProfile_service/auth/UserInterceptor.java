package com.company.MakeMyTrip.userProfile_service.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class UserInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        String userIdHeader = request.getHeader(USER_ID_HEADER);

        if (userIdHeader == null || userIdHeader.isBlank()) {

            log.warn(
                    "Missing {} header for {} {}",
                    USER_ID_HEADER,
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        try {

            Long userId = Long.parseLong(userIdHeader);

            if (userId <= 0) {

                log.warn(
                        "Invalid user ID received for {} {}",
                        request.getMethod(),
                        request.getRequestURI()
                );

                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }

            UserContextHolder.setCurrentUserId(userId);

            return true;

        } catch (NumberFormatException exception) {

            log.warn(
                    "Malformed user ID received for {} {}",
                    request.getMethod(),
                    request.getRequestURI()
            );

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception) {

        UserContextHolder.clear();
    }
}