package com.company.MakeMyTrip.booking_service.advices;

import com.company.MakeMyTrip.booking_service.exceptions.BookingModificationNotAllowedException;
import com.company.MakeMyTrip.booking_service.exceptions.BookingNotFoundException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidBookingStateException;
import com.company.MakeMyTrip.booking_service.exceptions.InvalidUserContextException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleBookingNotFound(
            BookingNotFoundException exception) {

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

    @ExceptionHandler(InvalidBookingStateException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidBookingState(
            InvalidBookingStateException exception) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        List<String> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .toList();

        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST)
                .message("Input validation failed")
                .subErrors(errors)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnexpectedException(
            Exception exception) {

        log.error(
                "Unexpected error while processing booking request",
                exception
        );

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    @ExceptionHandler(BookingModificationNotAllowedException.class)
    public ResponseEntity<ApiResponse<?>> handleBookingModificationNotAllowed(
            BookingModificationNotAllowedException exception) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(
            HttpStatus status,
            String message) {

        ApiError apiError = ApiError.builder()
                .status(status)
                .message(message)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(
            ApiError apiError) {

        return ResponseEntity
                .status(apiError.getStatus())
                .body(new ApiResponse<>(apiError));
    }
}