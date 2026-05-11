package com.axel.trainingmetricsapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionJpaRepository extends JpaRepository<TrainingSessionJpaEntity, Long> {
    List<TrainingSessionJpaEntity> findAllByAthleteId(long athleteId);
}
