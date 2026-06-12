package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.TrainingSession;

public interface GetTrainingSessionUseCase {
    TrainingSession execute(long sessionId, long athleteId, long coachId);
}
