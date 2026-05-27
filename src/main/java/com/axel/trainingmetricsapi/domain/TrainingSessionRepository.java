package com.axel.trainingmetricsapi.domain;

import java.util.Optional;

public interface TrainingSessionRepository {
    TrainingSession save(TrainingSession trainingSession);
    Optional<TrainingSession> findById(long id);
    PageResult<TrainingSession> findAllByAthleteId(long athleteId, int pageNumber, int pageSize);
    void deleteById(long id);
    boolean existsById(long id);
}
