package com.axel.trainingmetricsapi.domain.exception;

public class LoadReportNotFoundException extends ResourceNotFoundException {

    public LoadReportNotFoundException(long athleteId) {
        super("No load report found for athlete " + athleteId);
    }
}
