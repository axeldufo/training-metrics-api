package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;

import java.time.LocalDate;

public interface GetWeeklyWellnessByWeekUseCase {
    WeeklyWellness execute(long athleteId, long coachId, LocalDate weekStartDate);
}
