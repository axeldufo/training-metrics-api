package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.TrainingSession;

import java.util.List;

public interface TrainingSessionService {
    TrainingSession save(TrainingSession trainingSession);
    List<TrainingSession> findAllByAthleteId(long athleteId);
    TrainingSession findById(long id);
}
