package com.axel.trainingmetricsapi.shared.interfaces.web.dto;

public record ApiError(ErrorCode code, String field, String message) {
}
