package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

public interface UpdateWeeklyWellnessUseCase {
    WeeklyWellness execute(WeeklyWellness wellness, long coachId);
}
