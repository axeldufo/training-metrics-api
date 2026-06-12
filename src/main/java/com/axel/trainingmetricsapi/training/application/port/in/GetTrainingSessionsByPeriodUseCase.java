package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;

import java.time.LocalDate;
import java.util.List;

public interface GetTrainingSessionsByPeriodUseCase {
    List<TrainingSession> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
