package com.axel.trainingmetricsapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyWellnessJpaRepository extends JpaRepository<WeeklyWellnessJpaEntity, Long> {
    List<WeeklyWellnessJpaEntity> findAllByAthleteIdAndWeekStartDateBetween(long athleteId, LocalDate from, LocalDate to);
    boolean existsByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
}
