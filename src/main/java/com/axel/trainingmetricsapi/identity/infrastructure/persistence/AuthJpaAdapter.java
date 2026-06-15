package com.axel.trainingmetricsapi.identity.infrastructure.persistence;

import com.axel.trainingmetricsapi.identity.domain.AuthRepository;
import com.axel.trainingmetricsapi.identity.domain.CoachAuthData;
import com.axel.trainingmetricsapi.identity.domain.CoachCredentials;
import com.axel.trainingmetricsapi.identity.domain.exception.EmailAlreadyExistsException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AuthJpaAdapter implements AuthRepository {

    private final CoachJpaRepository coachJpaRepository;

    public AuthJpaAdapter(CoachJpaRepository coachJpaRepository) {
        this.coachJpaRepository = coachJpaRepository;
    }

    @Override
    public boolean existsByEmail(String email) {
        return coachJpaRepository.existsByEmail(email);
    }

    @Override
    public CoachAuthData register(CoachCredentials credentials, String hashedPassword) {
        CoachJpaEntity entityToPersist = CoachJpaEntity.builder()
            .name(credentials.name())
            .email(credentials.email())
            .hashedPassword(hashedPassword)
            .build();
        try {
            CoachJpaEntity entityPersisted = coachJpaRepository.save(entityToPersist);
            return new CoachAuthData(entityPersisted.getId(), entityPersisted.getHashedPassword());
        } catch (DataIntegrityViolationException e) {
            throw new EmailAlreadyExistsException(credentials.email());
        }
    }

    @Override
    public Optional<CoachAuthData> findByEmail(String email) {
        return coachJpaRepository.findByEmail(email)
            .map(entity -> new CoachAuthData(entity.getId(), entity.getHashedPassword()));
    }

}
