package com.axel.trainingmetricsapi.athlete.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteJpaRepository extends JpaRepository<AthleteJpaEntity, Long> {
    Page<AthleteJpaEntity> findAllByCoachId(long coachId, Pageable pageRequest);
    boolean existsByCoachId(long coachId);
}
