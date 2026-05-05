package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface CoachRepository {
    Coach save(Coach coach);
    Optional<Coach> findById(Long id);
    List<Coach> findAll();
    void deleteById(Long id);
    boolean existsById(Long coachId);
}
