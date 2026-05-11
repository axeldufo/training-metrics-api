package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface CoachRepository {
    Coach save(Coach coach);
    Optional<Coach> findById(long id);
    List<Coach> findAll();
    void deleteById(long id);
    boolean existsById(long id);
}
