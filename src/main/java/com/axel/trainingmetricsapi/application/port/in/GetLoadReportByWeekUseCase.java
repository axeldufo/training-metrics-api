package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.LoadReport;

import java.time.LocalDate;

public interface GetLoadReportByWeekUseCase {
    LoadReport execute(long athleteId, long coachId, LocalDate weekStartDate);
}
