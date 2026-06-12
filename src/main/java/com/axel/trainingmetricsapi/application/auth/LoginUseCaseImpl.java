package com.axel.trainingmetricsapi.application.auth;

import com.axel.trainingmetricsapi.application.port.in.LoginUseCase;
import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthRepository authRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public LoginUseCaseImpl(AuthRepository authRepository, PasswordEncoderPort passwordEncoderPort) {
        this.authRepository = authRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public CoachAuthData execute(String email, String rawPassword) {
        CoachAuthData coachAuthData = authRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoderPort.matches(rawPassword, coachAuthData.hashedPassword())) {
            throw new InvalidCredentialsException();
        }
        return coachAuthData;
    }
}
