package com.axel.trainingmetricsapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AthleteJpaRepository extends JpaRepository<AthleteJpaEntity, Long> {
    List<AthleteJpaEntity> findAllByCoachId(long coachId);
}
