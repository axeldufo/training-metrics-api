package com.axel.trainingmetricsapi.wellness.application.port.in;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;

public interface CreateWeeklyWellnessUseCase {
    WeeklyWellness execute(WeeklyWellness wellness, long coachId);
}
