package com.axel.trainingmetricsapi.athlete.interfaces.web.dto;

import com.axel.trainingmetricsapi.shared.domain.Sport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AthleteRequest (
    @NotBlank @Size(max=100) String firstName,
    @NotBlank @Size(max=100) String lastName,
    LocalDate birthDate,
    @NotNull Sport sport,
    Double weightInKg) {
}
