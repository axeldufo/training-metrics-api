package com.axel.trainingmetricsapi.athlete.application.port.in;

public interface DeleteAthleteUseCase {
    void execute(long athleteId, long coachId);
}
