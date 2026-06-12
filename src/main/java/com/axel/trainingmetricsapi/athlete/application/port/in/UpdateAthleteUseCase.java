package com.axel.trainingmetricsapi.athlete.application.port.in;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;

public interface UpdateAthleteUseCase {
    Athlete execute(Athlete athlete, long coachId);
}
