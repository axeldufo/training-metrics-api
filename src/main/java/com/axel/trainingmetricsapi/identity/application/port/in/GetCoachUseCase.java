package com.axel.trainingmetricsapi.identity.application.port.in;

import com.axel.trainingmetricsapi.identity.domain.Coach;

public interface GetCoachUseCase {
    Coach execute(long coachId);
}
