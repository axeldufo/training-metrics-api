package com.axel.trainingmetricsapi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TrainingSessionJpaRepository extends JpaRepository<TrainingSessionJpaEntity, Long> {
    List<TrainingSessionJpaEntity> findAllByAthleteIdAndDateBetween(long athleteId, LocalDate from, LocalDate to);
}
