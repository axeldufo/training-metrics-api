package com.axel.trainingmetricsapi.shared.interfaces.web;

import com.axel.trainingmetricsapi.shared.interfaces.web.exception.InvalidPeriodException;
import com.axel.trainingmetricsapi.shared.domain.exception.DomainValidationException;
import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<List<ApiError>> handleAthleteNotFound(ResourceNotFoundException exception) {
        ApiError apiError = new ApiError(ErrorCode.NOT_FOUND, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(apiError));
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<List<ApiError>> handleDomainValidationErrors(DomainValidationException exception) {
        ApiError apiError = new ApiError(ErrorCode.DOMAIN_VALIDATION_ERROR, null, exception.getMessage());
        return ResponseEntity.badRequest().body(List.of(apiError));
    }

    @ExceptionHandler(InvalidPeriodException.class)
    public ResponseEntity<List<ApiError>> handleInvalidPeriod(InvalidPeriodException exception) {
        return ResponseEntity.badRequest().body(
            List.of(new ApiError(ErrorCode.HTTP_VALIDATION_ERROR, "from", exception.getMessage())));
    }

    // Web infrastructure exception — handled here for consistent ApiError format across all 400 responses
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ApiError>> handleHttpValidationErrors(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<ApiError> apiErrors = fieldErrors.stream()
            .map(fieldError -> new ApiError(ErrorCode.HTTP_VALIDATION_ERROR, fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(apiErrors);
    }

    // Web infrastructure exception — handled here for consistent ApiError format across all 400 responses
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<List<ApiError>> handleMissingParams(MissingServletRequestParameterException exception) {
        ApiError error = new ApiError(ErrorCode.HTTP_VALIDATION_ERROR, exception.getParameterName(), exception.getMessage());
        return ResponseEntity.badRequest().body(List.of(error));
    }

    // Web infrastructure exception — handled here for consistent ApiError format across all 400 responses
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ApiError>> handleConstraintViolation(ConstraintViolationException exception) {
        List<ApiError> errors = exception.getConstraintViolations().stream()
            .map(violation -> new ApiError(
                ErrorCode.HTTP_VALIDATION_ERROR,
                extractFieldName(violation.getPropertyPath().toString()),
                violation.getMessage()))
            .toList();
        return ResponseEntity.badRequest().body(errors);
    }

    private String extractFieldName(String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}
