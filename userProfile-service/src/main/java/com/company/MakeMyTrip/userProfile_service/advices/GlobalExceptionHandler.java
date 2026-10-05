package com.company.MakeMyTrip.userProfile_service.advices;

import com.company.MakeMyTrip.userProfile_service.exceptions.InvalidUserContextException;
import com.company.MakeMyTrip.userProfile_service.exceptions.ProfileNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleProfileNotFound(
            ProfileNotFoundException exception) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidUserContextException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidUserContext(
            InvalidUserContextException exception) {

        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        List<String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .toList();

        ApiError apiError = ApiError.builder()
                .status(String.valueOf(HttpStatus.BAD_REQUEST))
                .message("Input validation failed")
                .subErrors(errors)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(
            Exception exception) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(
            HttpStatus status,
            String message) {

        ApiError apiError = ApiError.builder()
                .status(String.valueOf(status))
                .message(message)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(
            ApiError apiError) {

        return ResponseEntity
                .status(Integer.parseInt(apiError.getStatus()))
                .body(new ApiResponse<>(apiError));
    }
}