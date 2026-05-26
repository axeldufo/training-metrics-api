package com.axel.trainingmetricsapi.dto.request;

import com.axel.trainingmetricsapi.domain.Sport;
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
