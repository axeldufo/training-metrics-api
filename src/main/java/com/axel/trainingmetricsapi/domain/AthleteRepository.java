package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface AthleteRepository {
    Athlete save(Athlete athlete);
    Optional<Athlete> findById(Long id);
    List<Athlete> findAll();
    void deleteById(Long id);
    boolean existsById(Long athleteId);
}
