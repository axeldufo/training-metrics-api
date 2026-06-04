package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.WeeklyReport;

import java.time.LocalDate;
import java.util.List;

public interface WeeklyReportService {
    WeeklyReport getWeeklyReport(long athleteId, LocalDate weekStartDate);
    WeeklyReport getLatestWeeklyReport(long athleteId);
    List<WeeklyReport> getWeeklyReportsByPeriod(long athleteId, LocalDate from, LocalDate to);
}
