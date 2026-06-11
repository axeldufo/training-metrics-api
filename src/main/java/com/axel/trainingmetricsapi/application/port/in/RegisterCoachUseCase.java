package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;

public interface RegisterCoachUseCase {
    CoachAuthData execute(CoachCredentials credentials);
}
