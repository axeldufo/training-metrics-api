package com.axel.trainingmetricsapi.exception;

import com.axel.trainingmetricsapi.dto.response.ErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ErrorResponse>> handleValidationErrors(MethodArgumentNotValidException exception) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
        List<ErrorResponse> errorResponses = fieldErrors.stream()
            .map(fieldError -> new ErrorResponse(fieldError.getField(), fieldError.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest().body(errorResponses);
    }

}
