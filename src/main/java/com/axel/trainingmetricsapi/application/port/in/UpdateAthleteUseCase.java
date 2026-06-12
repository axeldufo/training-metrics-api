package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.Athlete;

public interface UpdateAthleteUseCase {
    Athlete execute(Athlete athlete, long coachId);
}
