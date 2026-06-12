package com.axel.trainingmetricsapi.application.port.in;

import com.axel.trainingmetricsapi.domain.CoachAuthData;

public interface LoginUseCase {
    CoachAuthData execute(String email, String rawPassword);
}
