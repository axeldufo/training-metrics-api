package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.interfaces.web.security.JwtUtils;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.RegisterRequest;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.AuthResponse;
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
            registerRequest.password()
        );
    }

    public AuthResponse toAuthResponse(CoachAuthData authData) {
        return new AuthResponse(jwtUtils.generateToken(authData.id()));
    }
}
