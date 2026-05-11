package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository {
    TrainingSession save(TrainingSession trainingSession);
    Optional<TrainingSession> findById(long id);
    List<TrainingSession> findAllByAthleteId(long athleteId);
    void deleteById(long id);
    boolean existsById(long id);
}
