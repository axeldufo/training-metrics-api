package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;

public interface CreateWeeklyWellnessUseCase {
    WeeklyWellness execute(WeeklyWellness wellness, long coachId);
}
