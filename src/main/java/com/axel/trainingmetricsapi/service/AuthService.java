package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;

public interface AuthService {
    CoachAuthData register(CoachCredentials credentials);
    CoachAuthData login(String email, String rawPassword);
}
