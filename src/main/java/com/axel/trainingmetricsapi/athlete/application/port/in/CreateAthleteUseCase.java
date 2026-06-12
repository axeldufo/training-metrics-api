package com.axel.trainingmetricsapi.athlete.application.port.in;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;

public interface CreateAthleteUseCase {
    Athlete execute(Athlete athlete);
}
