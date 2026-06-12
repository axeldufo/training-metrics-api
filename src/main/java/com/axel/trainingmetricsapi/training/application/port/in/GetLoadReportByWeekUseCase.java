package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.LoadReport;

import java.time.LocalDate;

public interface GetLoadReportByWeekUseCase {
    LoadReport execute(long athleteId, long coachId, LocalDate weekStartDate);
}
