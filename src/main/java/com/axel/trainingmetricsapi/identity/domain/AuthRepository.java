package com.axel.trainingmetricsapi.identity.domain;

import java.util.Optional;

public interface AuthRepository {
    boolean existsByEmail(String email);
    CoachAuthData register(CoachCredentials credentials, String hashedPassword);
    Optional<CoachAuthData> findByEmail(String email);
}
