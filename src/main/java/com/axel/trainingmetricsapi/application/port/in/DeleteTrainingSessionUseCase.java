package com.axel.trainingmetricsapi.application.port.in;

public interface DeleteTrainingSessionUseCase {
    void execute(long sessionId, long athleteId, long coachId);
}
