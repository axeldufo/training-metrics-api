package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface AthleteRepository {
    Athlete save(Athlete athlete);
    Optional<Athlete> findById(long id);
    List<Athlete> findAll();
    void deleteById(long id);
    boolean existsById(long id);
}
