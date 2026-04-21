package com.axel.trainingmetricsapi.exception;

public class AthleteNotFoundException extends RuntimeException {

    public AthleteNotFoundException(Long id) {
        super("Athlete not found with id: " + id);
    }

}
