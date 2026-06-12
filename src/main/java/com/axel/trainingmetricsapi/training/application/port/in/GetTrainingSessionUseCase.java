package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;

public interface GetTrainingSessionUseCase {
    TrainingSession execute(long sessionId, long athleteId, long coachId);
}
