package com.axel.trainingmetricsapi.identity.application.port.in;

import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;

public interface LoginUseCase {
    CoachAuthData execute(String email, String rawPassword);
}
