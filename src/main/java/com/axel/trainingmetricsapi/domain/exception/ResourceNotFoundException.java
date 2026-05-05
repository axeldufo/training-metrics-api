package com.axel.trainingmetricsapi.domain.exception;

public abstract class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

}
