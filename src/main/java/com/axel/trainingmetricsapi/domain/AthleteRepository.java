package com.axel.trainingmetricsapi.domain;

import java.util.Optional;

public interface AthleteRepository {
    Athlete save(Athlete athlete);
    Optional<Athlete> findById(long id);
    PageResult<Athlete> findAllByCoachId(long coachId, int pageNumber, int pageSize);
    void deleteById(long id);
    boolean existsById(long id);
}
