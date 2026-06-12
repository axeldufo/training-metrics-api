package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.TrainingSession;

public interface CreateTrainingSessionUseCase {
    TrainingSession execute(TrainingSession session, long coachId);
}
