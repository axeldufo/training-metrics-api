package com.axel.trainingmetricsapi.domain.exception;

public class WeeklyWellnessNotFoundException extends ResourceNotFoundException {

    public WeeklyWellnessNotFoundException(long id) {
        super("WeeklyWellness not found: " + id);
    }
}
