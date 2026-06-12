package com.axel.trainingmetricsapi.athlete.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

public class AthleteNotFoundException extends ResourceNotFoundException {

    public AthleteNotFoundException(long id) {
        super("Athlete not found with id: " + id);
    }

}
