package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.application.port.out.PasswordEncoderPort;
import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
import com.axel.trainingmetricsapi.domain.exception.EmailAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.InvalidCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoderPort passwordEncoder;

    public AuthServiceImpl(AuthRepository authRepository, PasswordEncoderPort passwordEncoder) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public CoachAuthData register(CoachCredentials credentials) {
        String email = credentials.email();
        if (authRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        String hashedPassword = passwordEncoder.encode(credentials.rawPassword());

        return authRepository.register(credentials, hashedPassword);
    }

    @Override
    public CoachAuthData login(String email, String rawPassword) {
        CoachAuthData coachAuthData = authRepository.findByEmail(email)
            .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(rawPassword, coachAuthData.hashedPassword())) {
            throw new InvalidCredentialsException();
        }
        return coachAuthData;
    }

}
