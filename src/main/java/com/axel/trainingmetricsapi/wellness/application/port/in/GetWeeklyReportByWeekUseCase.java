package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;

import java.time.LocalDate;

public interface GetWeeklyReportByWeekUseCase {
    WeeklyReport execute(long athleteId, long coachId, LocalDate weekStartDate);
}
