package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

public interface GetWeeklyWellnessUseCase {
    WeeklyWellness execute(long wellnessId, long athleteId, long coachId);
}
