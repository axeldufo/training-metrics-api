package com.axel.trainingmetricsapi.wellness.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

import java.time.LocalDate;

public class WeeklyReportNotFoundException extends ResourceNotFoundException {

    public WeeklyReportNotFoundException(long athleteId, LocalDate weekStartDate) {
        super("No weekly report available for athlete " + athleteId + " on week " + weekStartDate);
    }

    public WeeklyReportNotFoundException(long athleteId) {
        super("No weekly report available for athlete " + athleteId);
    }
}
