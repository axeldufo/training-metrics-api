package com.axel.trainingmetricsapi.training.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoadReportRepository {
    LoadReport save(LoadReport report);
    Optional<LoadReport> findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
    Optional<LoadReport> findLatestByAthleteId(long athleteId);
    List<LoadReport> findByAthleteIdAndWeekStartDateBetween(long athleteId, LocalDate from, LocalDate to);
    void deleteByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
}
