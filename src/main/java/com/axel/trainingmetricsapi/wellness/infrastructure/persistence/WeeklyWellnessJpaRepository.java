package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyWellnessJpaRepository extends JpaRepository<WeeklyWellnessJpaEntity, Long> {
    List<WeeklyWellnessJpaEntity> findAllByAthleteIdAndWeekStartDateBetween(long athleteId, LocalDate from, LocalDate to);
    Optional<WeeklyWellnessJpaEntity> findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
    Optional<WeeklyWellnessJpaEntity> findTopByAthleteIdOrderByWeekStartDateDesc(long athleteId);
    boolean existsByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
}
