package com.axel.trainingmetricsapi.domain;

import java.util.List;
import java.util.Optional;

public interface CoachRepository {
    Optional<Coach> findById(long id);
    List<Coach> findAll();
    void deleteById(long id);
    boolean existsById(long id);
    void updateName(long id, String name);
}
