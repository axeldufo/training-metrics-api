package com.axel.trainingmetricsapi.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingSessionJpaRepository extends JpaRepository<TrainingSessionJpaEntity, Long> {
    Page<TrainingSessionJpaEntity> findAllByAthleteId(long athleteId, Pageable pageRequest);
}
