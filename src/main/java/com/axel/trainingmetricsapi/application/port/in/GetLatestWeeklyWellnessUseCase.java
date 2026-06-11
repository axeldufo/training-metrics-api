package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

public interface GetLatestWeeklyWellnessUseCase {
    WeeklyWellness execute(long athleteId, long coachId);
}
