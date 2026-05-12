package com.axel.trainingmetricsapi.domain.exception;

public class CoachNotFoundException extends ResourceNotFoundException {

    public CoachNotFoundException(long id) {
        super("Coach not found with id: " + id);
    }

}
