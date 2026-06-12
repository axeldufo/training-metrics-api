package com.axel.trainingmetricsapi.training.domain.exception;

import com.axel.trainingmetricsapi.shared.domain.exception.ResourceNotFoundException;

public class TrainingSessionNotFoundException extends ResourceNotFoundException {

    public TrainingSessionNotFoundException(long id) {
        super("Training session not found with id: " + id);
    }

}
