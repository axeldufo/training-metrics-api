package com.axel.trainingmetricsapi.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingSessionRepository {
    TrainingSession save(TrainingSession trainingSession);
    Optional<TrainingSession> findById(long id);
    List<TrainingSession> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to);
    void deleteById(long id);
    boolean existsById(long id);
}
