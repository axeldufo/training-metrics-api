package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;

public interface GetLatestWeeklyWellnessUseCase {
    WeeklyWellness execute(long athleteId, long coachId);
}
