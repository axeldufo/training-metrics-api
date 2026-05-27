package com.axel.trainingmetricsapi.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyWellnessRepository {
    WeeklyWellness save(WeeklyWellness wellness);
    Optional<WeeklyWellness> findById(long id);
    List<WeeklyWellness> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to);
    void deleteById(long id);
    boolean existsById(long id);
    boolean existsByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
}
