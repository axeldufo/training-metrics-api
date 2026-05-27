package com.axel.trainingmetricsapi.domain;

import java.util.Optional;

public interface CoachRepository {
    Optional<Coach> findById(long id);
    void deleteById(long id);
    boolean existsById(long id);
    void updateName(long id, String name);
}
