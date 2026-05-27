package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyWellnessService {
    WeeklyWellness save(WeeklyWellness wellness);
    List<WeeklyWellness> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to);
    WeeklyWellness findById(long id, long athleteId);
    WeeklyWellness update(WeeklyWellness wellness);
    void deleteById(long id, long athleteId);
}
