package com.axel.trainingmetricsapi.application.auth;

import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.domain.exception.EmailAlreadyExistsException;
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
class RegisterCoachUseCaseImplTest {

    @Mock
    private AuthRepository authRepository;

    @Mock
    private PasswordEncoderPort passwordEncoderPort;

    @InjectMocks
    private RegisterCoachUseCaseImpl useCase;

    @Test
    void execute_shouldPersistEncodedCredentials_whenEmailDoesntExists() {
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        String email = credentials.email();
        when(authRepository.existsByEmail(email)).thenReturn(false);
        String hashedPassword = "hashedKeyXYZ";
        when(passwordEncoderPort.encode(credentials.rawPassword())).thenReturn(hashedPassword);
        CoachAuthData authDataFromRepository = Instancio.create(CoachAuthData.class);
        when(authRepository.register(credentials, hashedPassword)).thenReturn(authDataFromRepository);

        CoachAuthData result = useCase.execute(credentials);

        verify(authRepository).existsByEmail(email);
        verify(passwordEncoderPort).encode(credentials.rawPassword());
        verify(authRepository).register(credentials, hashedPassword);
        assertThat(result).isEqualTo(authDataFromRepository);
    }

    @Test
    void execute_shouldThrowException_whenEmailAlreadyExists() {
        CoachCredentials credentials = Instancio.create(CoachCredentials.class);
        String email = credentials.email();
        when(authRepository.existsByEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(credentials))
            .isInstanceOf(EmailAlreadyExistsException.class);

        verify(authRepository).existsByEmail(email);
        verify(passwordEncoderPort, never()).encode(any());
        verify(authRepository, never()).register(any(), any());
    }
}
