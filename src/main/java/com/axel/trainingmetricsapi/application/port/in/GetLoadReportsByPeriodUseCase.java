package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.LoadReport;

import java.time.LocalDate;
import java.util.List;

public interface GetLoadReportsByPeriodUseCase {
    List<LoadReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
