package com.axel.trainingmetricsapi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoadReportJpaRepository extends JpaRepository<LoadReportJpaEntity, Long> {
    Optional<LoadReportJpaEntity> findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
    Optional<LoadReportJpaEntity> findFirstByAthleteIdOrderByWeekStartDateDesc(long athleteId);
    List<LoadReportJpaEntity> findAllByAthleteIdAndWeekStartDateBetween(long athleteId, LocalDate from, LocalDate to);
    void deleteByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
}
