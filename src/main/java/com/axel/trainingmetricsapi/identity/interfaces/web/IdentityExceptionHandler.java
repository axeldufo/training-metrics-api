package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.domain.exception.CoachHasAthletesException;
import com.axel.trainingmetricsapi.identity.domain.exception.EmailAlreadyExistsException;
import com.axel.trainingmetricsapi.identity.domain.exception.InvalidCredentialsException;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ApiError;
import com.axel.trainingmetricsapi.shared.interfaces.web.dto.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class IdentityExceptionHandler {

    @ExceptionHandler(CoachHasAthletesException.class)
    public ResponseEntity<List<ApiError>> handleCoachHasAthletes(CoachHasAthletesException exception) {
        ApiError apiError = new ApiError(ErrorCode.COACH_HAS_ATHLETES, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(List.of(apiError));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<List<ApiError>> handleEmailAlreadyExists(EmailAlreadyExistsException exception) {
        ApiError apiError = new ApiError(ErrorCode.EMAIL_ALREADY_EXISTS, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(List.of(apiError));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<List<ApiError>> handleInvalidCredentials(InvalidCredentialsException exception) {
        ApiError apiError = new ApiError(ErrorCode.INVALID_CREDENTIALS, null, exception.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of(apiError));
    }
}
