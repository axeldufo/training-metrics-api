package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

import java.time.LocalDate;

public interface GetWeeklyWellnessByWeekUseCase {
    WeeklyWellness execute(long athleteId, long coachId, LocalDate weekStartDate);
}
