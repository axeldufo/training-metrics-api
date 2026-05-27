package com.axel.trainingmetricsapi.domain;

import java.util.List;

public record PageResult<T> (
    List<T> content,
    long totalElements,
    int pageNumber,
    int pageSize
) {
}
