package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.LoadReport;

import java.time.LocalDate;
import java.util.List;

public interface LoadReportService {
    LoadReport findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate);
    LoadReport findLatestByAthleteId(long athleteId);
    List<LoadReport> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to);
}
