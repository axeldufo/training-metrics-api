package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyReport;

import java.time.LocalDate;
import java.util.List;

public interface GetWeeklyReportsByPeriodUseCase {
    List<WeeklyReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
