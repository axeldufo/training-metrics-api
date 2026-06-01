package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.JwtUtils;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.dto.request.RegisterRequest;
import com.axel.trainingmetricsapi.dto.response.AuthResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthWebMapperTest {

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthWebMapper authWebMapper;

    @Test
    void toCredentials_shouldMapAllFields() {
        RegisterRequest registerRequest = Instancio.create(RegisterRequest.class);

        CoachCredentials coachCredentials = authWebMapper.toCredentials(registerRequest);

        assertThat(coachCredentials.name()).isEqualTo(registerRequest.name());
        assertThat(coachCredentials.email()).isEqualTo(registerRequest.email());
        assertThat(coachCredentials.rawPassword()).isEqualTo(registerRequest.password());
    }

    @Test
    void toAuthResponse_shouldMapAllFields() {
        CoachAuthData authData = Instancio.create(CoachAuthData.class);
        long coachId = authData.id();
        String expectedToken = "header.payload.signature";
        when(jwtUtils.generateToken(coachId)).thenReturn(expectedToken);

        AuthResponse authResponse = authWebMapper.toAuthResponse(authData);

        verify(jwtUtils).generateToken(coachId);
        assertThat(authResponse.token()).isEqualTo(expectedToken);
    }

}
