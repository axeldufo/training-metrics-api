package com.axel.trainingmetricsapi.training.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

public class LoadReportNotFoundException extends ResourceNotFoundException {

    public LoadReportNotFoundException(long athleteId) {
        super("No load report found for athlete " + athleteId);
    }
}
