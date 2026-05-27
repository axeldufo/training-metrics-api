package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.TrainingSession;

public interface TrainingSessionService {
    TrainingSession save(TrainingSession trainingSession);
    PageResult<TrainingSession> findAllByAthleteId(long athleteId, int pageNumber, int pageSize);
    TrainingSession findById(long id, long athleteId);
    TrainingSession update(TrainingSession trainingSession);
    void deleteById(long id, long athleteId);
}
