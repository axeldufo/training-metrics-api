package com.axel.trainingmetricsapi.training.interfaces.web.dto;

import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.training.domain.TargetZone;

import java.time.LocalDate;

public record TrainingSessionResponse(
    long id,
    LocalDate date,
    Sport sport,
    int rpe,
    int durationInMin,
    TargetZone targetZone,
    long athleteId,
    boolean aboveTargetAlert,
    boolean belowTargetAlert) {
}
