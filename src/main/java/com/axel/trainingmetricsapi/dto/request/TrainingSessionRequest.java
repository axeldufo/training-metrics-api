package com.axel.trainingmetricsapi.dto.request;

import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public record TrainingSessionRequest(
    @NotNull @PastOrPresent LocalDate date,
    @NotNull Sport sport,
    @NotNull @Min(value = 1) @Max(value = 10) Integer rpe,
    @NotNull @Min(value = 1) Integer durationInMin,
    @NotNull TargetZone targetZone) {
}
