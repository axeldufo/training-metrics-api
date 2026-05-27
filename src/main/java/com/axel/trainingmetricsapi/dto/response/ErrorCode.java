package com.axel.trainingmetricsapi.dto.response;

public enum ErrorCode {
    HTTP_VALIDATION_ERROR,   // Jakarta validation on request DTOs
    DOMAIN_VALIDATION_ERROR, // domain invariants
    NOT_FOUND,               // resource not found
    EMAIL_ALREADY_EXISTS,    // coach email must be unique across all accounts
    INVALID_CREDENTIALS,     // wrong email or password
    WELLNESS_ALREADY_EXISTS  // one wellness entry per athlete per week
}
