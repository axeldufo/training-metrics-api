package com.axel.trainingmetricsapi.exception;

import com.axel.trainingmetricsapi.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ApiError>> handleValidationErrors(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<ApiError> apiErrors = fieldErrors.stream()
            .map(fieldError -> new ApiError("VALIDATION_ERROR", fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(apiErrors);
    }

    @ExceptionHandler(AthleteNotFoundException.class)
    public ResponseEntity<List<ApiError>> handleAthleteNotFound(AthleteNotFoundException exception) {
        ApiError apiError = new ApiError("NOT_FOUND", null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(apiError));
    }

}
