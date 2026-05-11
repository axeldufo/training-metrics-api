package com.axel.trainingmetricsapi.dto.response;

public enum ErrorCode {
    HTTP_VALIDATION_ERROR,   // Jakarta validation on request DTOs
    DOMAIN_VALIDATION_ERROR, // domain invariants
    NOT_FOUND                // resource not found
}
