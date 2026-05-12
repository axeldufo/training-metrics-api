package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.TrainingSession;

import java.util.List;

public interface TrainingSessionService {
    List<TrainingSession> findAllByAthleteId(long athleteId);
    TrainingSession save(TrainingSession trainingSession);
    TrainingSession findById(long trainingSessionId);
    TrainingSession update(TrainingSession trainingSession);
    void deleteById(long trainingSessionId);
}
