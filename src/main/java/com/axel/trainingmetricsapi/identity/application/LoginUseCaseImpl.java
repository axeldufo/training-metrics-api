package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.application.port.in.LoginUseCase;
import com.axel.trainingmetricsapi.identity.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.identity.domain.AuthRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.exception.InvalidCredentialsException;
import com.axel.trainingmetricsapi.shared.application.annotation.SensitiveArg;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private final AuthRepository authRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public LoginUseCaseImpl(AuthRepository authRepository, PasswordEncoderPort passwordEncoderPort) {
        this.authRepository = authRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.login")
    public CoachAuthData execute(String email, @SensitiveArg String rawPassword) {
        CoachAuthData coachAuthData = authRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoderPort.matches(rawPassword, coachAuthData.hashedPassword())) {
            throw new InvalidCredentialsException();
        }
        return coachAuthData;
    }
}
