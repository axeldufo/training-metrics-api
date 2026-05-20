package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.dto.request.RegisterRequest;
import com.axel.trainingmetricsapi.dto.response.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthWebMapper {

    private final JwtUtils jwtUtils;

    public AuthWebMapper(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    public CoachCredentials toCredentials(RegisterRequest registerRequest) {
        return new CoachCredentials(
            registerRequest.name(),
            registerRequest.email(),
            registerRequest.rawPassword()
        );
    }

    public AuthResponse toAuthResponse(CoachAuthData authData) {
        return new AuthResponse(jwtUtils.generateToken(authData.id()));
    }
}
