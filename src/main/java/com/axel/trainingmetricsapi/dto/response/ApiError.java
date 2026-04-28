package com.axel.trainingmetricsapi.dto.response;

public record ApiError(ErrorCode code, String field, String message) {
}
