package com.axel.trainingmetricsapi.identity.interfaces.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoachUpdateRequest(
    @NotBlank @Size(max=100) String name) {
}
