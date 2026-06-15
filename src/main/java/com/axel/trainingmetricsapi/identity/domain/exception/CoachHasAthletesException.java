package com.axel.trainingmetricsapi.identity.domain.exception;

public class CoachHasAthletesException extends RuntimeException {

    public CoachHasAthletesException(long coachId) {
        super("Coach with id " + coachId + " still has athletes and cannot be deleted");
    }
}
