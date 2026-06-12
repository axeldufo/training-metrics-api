package com.axel.trainingmetricsapi.training.application.port.in;

public interface DeleteTrainingSessionUseCase {
    void execute(long sessionId, long athleteId, long coachId);
}
