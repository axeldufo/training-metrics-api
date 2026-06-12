package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.Athlete;

public interface CreateAthleteUseCase {
    Athlete execute(Athlete athlete);
}
