package com.axel.trainingmetricsapi.domain;

public class AthleteNotFoundException extends RuntimeException {

    public AthleteNotFoundException(Long id) {
        super("Athlete not found with id: " + id);
    }

}
