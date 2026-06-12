package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.Coach;

public interface GetCoachUseCase {
    Coach execute(long coachId);
}
