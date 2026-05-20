package com.axel.trainingmetricsapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoachUpdateRequest(
    @NotBlank @Size(max=100) String name) {
}
