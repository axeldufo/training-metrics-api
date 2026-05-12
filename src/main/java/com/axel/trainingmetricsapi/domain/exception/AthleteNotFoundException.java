package com.axel.trainingmetricsapi.domain.exception;

public class AthleteNotFoundException extends ResourceNotFoundException {

    public AthleteNotFoundException(long id) {
        super("Athlete not found with id: " + id);
    }

}
