package com.axel.trainingmetricsapi.domain.exception;

public class CoachNotFoundException extends ResourceNotFoundException {

    public CoachNotFoundException(Long id) {
        super("Coach not found with id: " + id);
    }
}
