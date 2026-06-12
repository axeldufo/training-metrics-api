package com.axel.trainingmetricsapi.wellness.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

public class WeeklyWellnessNotFoundException extends ResourceNotFoundException {

    public WeeklyWellnessNotFoundException(long id) {
        super("WeeklyWellness not found: " + id);
    }
}
