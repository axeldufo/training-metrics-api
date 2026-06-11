package com.axel.trainingmetricsapi.application.auth;

import com.axel.trainingmetricsapi.application.port.in.RegisterCoachUseCase;
import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.domain.exception.EmailAlreadyExistsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RegisterCoachUseCaseImpl implements RegisterCoachUseCase {

    private final AuthRepository authRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public RegisterCoachUseCaseImpl(AuthRepository authRepository, PasswordEncoderPort passwordEncoderPort) {
        this.authRepository = authRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public CoachAuthData execute(CoachCredentials credentials) {
        String email = credentials.email();
        if (authRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
        String hashedPassword = passwordEncoderPort.encode(credentials.rawPassword());
        return authRepository.register(credentials, hashedPassword);
    }
}
