package com.axel.trainingmetricsapi.shared.interfaces.web.dto;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    long totalElements,
    int page,
    int size
) {
}
