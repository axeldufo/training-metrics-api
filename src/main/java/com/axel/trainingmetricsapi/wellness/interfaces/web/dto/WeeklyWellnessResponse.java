package com.axel.trainingmetricsapi.wellness.interfaces.web.dto;

import java.time.LocalDate;

public record WeeklyWellnessResponse(
    long id,
    long athleteId,
    LocalDate weekStartDate,
    int perceivedDifficulty,
    int perceivedFatigue,
    int motivation) {
}
