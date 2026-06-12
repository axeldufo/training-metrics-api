package com.axel.trainingmetricsapi.identity.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

public class CoachNotFoundException extends ResourceNotFoundException {

    public CoachNotFoundException(long id) {
        super("Coach not found with id: " + id);
    }

}
