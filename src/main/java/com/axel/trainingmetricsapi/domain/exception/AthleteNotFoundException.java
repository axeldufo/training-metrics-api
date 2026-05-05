package com.axel.trainingmetricsapi.domain.exception;

public class AthleteNotFoundException extends ResourceNotFoundException {

    public AthleteNotFoundException(Long id) {
        super("Athlete not found with id: " + id);
    }

}
