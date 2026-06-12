package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;

public interface UpdateTrainingSessionUseCase {
    TrainingSession execute(TrainingSession session, long coachId);
}
