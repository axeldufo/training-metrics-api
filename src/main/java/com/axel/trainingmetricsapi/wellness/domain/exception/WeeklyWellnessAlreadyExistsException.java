package com.axel.trainingmetricsapi.wellness.domain.exception;

import java.time.LocalDate;

public class WeeklyWellnessAlreadyExistsException extends RuntimeException {

    public WeeklyWellnessAlreadyExistsException(long athleteId, LocalDate weekStartDate) {
        super("A wellness entry already exists for athlete " + athleteId + " on week " + weekStartDate);
    }
}
