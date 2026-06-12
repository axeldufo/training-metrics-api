package com.axel.trainingmetricsapi.identity.application.port.in;

import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;

public interface RegisterCoachUseCase {
    CoachAuthData execute(CoachCredentials credentials);
}
