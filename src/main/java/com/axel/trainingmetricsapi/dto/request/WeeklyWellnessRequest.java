package com.axel.trainingmetricsapi.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WeeklyWellnessRequest(
    @NotNull LocalDate weekStartDate,
    @NotNull @Min(1) @Max(5) Integer perceivedDifficulty,
    @NotNull @Min(1) @Max(5) Integer perceivedFatigue,
    @NotNull @Min(1) @Max(5) Integer motivation) {
}
