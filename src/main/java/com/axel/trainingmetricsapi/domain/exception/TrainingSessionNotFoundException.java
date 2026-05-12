package com.axel.trainingmetricsapi.domain.exception;

public class TrainingSessionNotFoundException extends ResourceNotFoundException {

    public TrainingSessionNotFoundException(long id) {
        super("Training session not found with id: " + id);
    }

}
