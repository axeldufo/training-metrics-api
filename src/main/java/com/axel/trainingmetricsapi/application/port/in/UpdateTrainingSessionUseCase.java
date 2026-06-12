package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.TrainingSession;

public interface UpdateTrainingSessionUseCase {
    TrainingSession execute(TrainingSession session, long coachId);
}
