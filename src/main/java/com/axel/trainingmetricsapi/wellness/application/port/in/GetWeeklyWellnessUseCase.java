package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;

public interface GetWeeklyWellnessUseCase {
    WeeklyWellness execute(long wellnessId, long athleteId, long coachId);
}
