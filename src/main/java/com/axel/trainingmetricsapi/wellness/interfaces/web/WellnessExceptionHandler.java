package com.axel.trainingmetricsapi.wellness.interfaces.web;

import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyWellnessAlreadyExistsException;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class WellnessExceptionHandler {

    @ExceptionHandler(WeeklyWellnessAlreadyExistsException.class)
    public ResponseEntity<List<ApiError>> handleWeeklyWellnessAlreadyExists(WeeklyWellnessAlreadyExistsException exception) {
        ApiError apiError = new ApiError(ErrorCode.WELLNESS_ALREADY_EXISTS, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(List.of(apiError));
    }
}
