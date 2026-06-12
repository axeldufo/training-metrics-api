package com.axel.trainingmetricsapi.wellness.interfaces.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record WeeklyWellnessRequest(
    @NotNull @PastOrPresent LocalDate weekStartDate,
    @NotNull @Min(1) @Max(5) Integer perceivedDifficulty,
    @NotNull @Min(1) @Max(5) Integer perceivedFatigue,
    @NotNull @Min(1) @Max(5) Integer motivation) {
}
