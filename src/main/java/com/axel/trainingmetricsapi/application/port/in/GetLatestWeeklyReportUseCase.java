package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyReport;

public interface GetLatestWeeklyReportUseCase {
    WeeklyReport execute(long athleteId, long coachId);
}
