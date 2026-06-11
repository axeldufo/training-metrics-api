package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.TrainingSession;

import java.time.LocalDate;
import java.util.List;

public interface GetTrainingSessionsByPeriodUseCase {
    List<TrainingSession> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
