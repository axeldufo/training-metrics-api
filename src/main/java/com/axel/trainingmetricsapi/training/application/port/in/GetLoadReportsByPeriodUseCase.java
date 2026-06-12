package com.axel.trainingmetricsapi.training.application.port.in;

import com.axel.trainingmetricsapi.training.domain.LoadReport;

import java.time.LocalDate;
import java.util.List;

public interface GetLoadReportsByPeriodUseCase {
    List<LoadReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
