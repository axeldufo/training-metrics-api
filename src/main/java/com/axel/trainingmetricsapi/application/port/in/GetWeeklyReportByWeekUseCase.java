package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyReport;

import java.time.LocalDate;

public interface GetWeeklyReportByWeekUseCase {
    WeeklyReport execute(long athleteId, long coachId, LocalDate weekStartDate);
}
