package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.TrainingSession;

import java.time.LocalDate;
import java.util.List;

public interface TrainingSessionService {
    TrainingSession save(TrainingSession trainingSession);
    List<TrainingSession> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to);
    TrainingSession findById(long id, long athleteId);
    TrainingSession update(TrainingSession trainingSession);
    void deleteById(long id, long athleteId);
}
