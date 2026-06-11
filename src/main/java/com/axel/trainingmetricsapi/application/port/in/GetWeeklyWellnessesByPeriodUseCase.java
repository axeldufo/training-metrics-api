package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

import java.time.LocalDate;
import java.util.List;

public interface GetWeeklyWellnessesByPeriodUseCase {
    List<WeeklyWellness> execute(long athleteId, long coachId, LocalDate from, LocalDate to);
}
