package com.axel.trainingmetricsapi.dto.response;

public record ApiError(String code, String field, String message) {
}
