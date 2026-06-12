package com.axel.trainingmetricsapi.shared.domain.exception;

public abstract class ResourceNotFoundException extends RuntimeException {

    protected ResourceNotFoundException(String message) {
        super(message);
    }

}
