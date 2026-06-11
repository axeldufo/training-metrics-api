package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.Athlete;

public interface GetAthleteUseCase {
    Athlete execute(long athleteId, long coachId);
}
