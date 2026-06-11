package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.domain.exception.EmailAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.InvalidCredentialsException;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_shouldPersistEncodedCredentials_whenEmailDoesntExists() {
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        String email = credentials.email();
        when(authRepository.existsByEmail(email)).thenReturn(false);
        String rawPassword = credentials.rawPassword();
        String hashedPassword = "hashedKeyXYZ";
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        CoachAuthData authDataFromRepository = Instancio.create(CoachAuthData.class);
        when(authRepository.register(credentials, hashedPassword)).thenReturn(authDataFromRepository);

        CoachAuthData authDataToReturn = authService.register(credentials);

        verify(authRepository).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(authRepository).register(credentials, hashedPassword);
        assertThat(authDataToReturn).isEqualTo(authDataFromRepository);
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        String email = credentials.email();
        when(authRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(credentials))
            .isInstanceOf(EmailAlreadyExistsException.class);

        verify(authRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(credentials.rawPassword());
        verify(authRepository, never()).register(any(), any());
    }

    @Test
    void login_shouldReturnCoachAuthData_whenCredentialsAreValid() {
        String email = "coach@test.com";
        CoachAuthData authDataFromRepository = Instancio.create(CoachAuthData.class);
        when(authRepository.findByEmail(email)).thenReturn(Optional.of(authDataFromRepository));
        String rawPassword = "rawPassword";
        String hashedPassword = authDataFromRepository.hashedPassword();
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        CoachAuthData authDataToReturn = authService.login(email, rawPassword);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
        assertThat(authDataToReturn).isEqualTo(authDataFromRepository);
    }

    @Test
    void login_shouldThrowException_whenEmailNotFound() {
        String email = "coach@test.com";
        when(authRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(email, "rawPassword"))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_shouldThrowException_whenPasswordDoesNotMatch() {
        String email = "coach@test.com";
        CoachAuthData authDataFromRepository = Instancio.create(CoachAuthData.class);
        when(authRepository.findByEmail(email)).thenReturn(Optional.of(authDataFromRepository));
        String rawPassword = "rawPassword";
        String hashedPassword = authDataFromRepository.hashedPassword();
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        assertThatThrownBy(() -> authService.login(email, "rawPassword"))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoder).matches(rawPassword, hashedPassword);
    }

}
