package com.axel.trainingmetricsapi.interfaces.web.dto.response;

import java.util.List;

public record PagedResponse<T>(
    List<T> content,
    long totalElements,
    int page,
    int size
) {
}
