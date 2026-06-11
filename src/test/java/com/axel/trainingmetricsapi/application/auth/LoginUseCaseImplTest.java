package com.axel.trainingmetricsapi.application.auth;

import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private LoginUseCaseImpl useCase;

    @Test
    void execute_shouldReturnCoachAuthData_whenCredentialsAreValid() {
        String email = "coach@test.com";
        CoachAuthData authData = Instancio.create(CoachAuthData.class);
        when(authRepository.findByEmail(email)).thenReturn(Optional.of(authData));
        String rawPassword = "rawPassword";
        when(passwordEncoderPort.matches(rawPassword, authData.hashedPassword())).thenReturn(true);

        CoachAuthData result = useCase.execute(email, rawPassword);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoderPort).matches(rawPassword, authData.hashedPassword());
        assertThat(result).isEqualTo(authData);
    }

    @Test
    void execute_shouldThrowException_whenEmailNotFound() {
        String email = "coach@test.com";
        when(authRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(email, "rawPassword"))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoderPort, never()).matches(any(), any());
    }

    @Test
    void execute_shouldThrowException_whenPasswordDoesNotMatch() {
        String email = "coach@test.com";
        CoachAuthData authData = Instancio.create(CoachAuthData.class);
        when(authRepository.findByEmail(email)).thenReturn(Optional.of(authData));
        String rawPassword = "rawPassword";
        when(passwordEncoderPort.matches(rawPassword, authData.hashedPassword())).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(email, rawPassword))
            .isInstanceOf(InvalidCredentialsException.class);

        verify(authRepository).findByEmail(email);
        verify(passwordEncoderPort).matches(rawPassword, authData.hashedPassword());
    }
}
