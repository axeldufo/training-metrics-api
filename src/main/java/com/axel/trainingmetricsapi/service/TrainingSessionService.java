package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.TrainingSession;

import java.util.List;

public interface TrainingSessionService {
    TrainingSession save(TrainingSession trainingSession);
    List<TrainingSession> findAllByAthleteId(long athleteId);
    TrainingSession findById(long id, long athleteId);
    TrainingSession update(TrainingSession trainingSession);
    void deleteById(long id, long athleteId);
}
