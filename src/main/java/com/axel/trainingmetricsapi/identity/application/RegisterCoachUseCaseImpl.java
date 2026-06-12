package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.application.port.in.RegisterCoachUseCase;
import com.axel.trainingmetricsapi.identity.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.identity.domain.AuthRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;
import com.axel.trainingmetricsapi.identity.domain.exception.EmailAlreadyExistsException;
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
