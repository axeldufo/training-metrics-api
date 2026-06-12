package com.axel.trainingmetricsapi.interfaces.web.dto.response;

public record ApiError(ErrorCode code, String field, String message) {
}
