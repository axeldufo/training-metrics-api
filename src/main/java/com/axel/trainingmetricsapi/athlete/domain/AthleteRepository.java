package com.axel.trainingmetricsapi.athlete.domain;

import com.axel.trainingmetricsapi.shared.domain.PageResult;

import java.util.Optional;

public interface AthleteRepository {
    Athlete save(Athlete athlete);
    Optional<Athlete> findById(long id);
    PageResult<Athlete> findAllByCoachId(long coachId, int pageNumber, int pageSize);
    void deleteById(long id);
    boolean existsById(long id);
}
