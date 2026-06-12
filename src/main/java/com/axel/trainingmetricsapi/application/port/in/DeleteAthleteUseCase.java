package com.axel.trainingmetricsapi.application.port.in;

public interface DeleteAthleteUseCase {
    void execute(long athleteId, long coachId);
}
