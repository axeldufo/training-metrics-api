package com.axel.trainingmetricsapi.interfaces.web.dto.response;

import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.domain.TargetZone;

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
