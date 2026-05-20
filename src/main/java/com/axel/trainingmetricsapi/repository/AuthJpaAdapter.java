package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.AuthRepository;
import com.axel.trainingmetricsapi.domain.CoachAuthData;
import com.axel.trainingmetricsapi.domain.CoachCredentials;
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
        CoachJpaEntity entityPersisted = coachJpaRepository.save(entityToPersist);
        return new CoachAuthData(entityPersisted.getId(), entityPersisted.getHashedPassword());
    }

    @Override
    public Optional<CoachAuthData> findByEmail(String email) {
        return coachJpaRepository.findByEmail(email)
            .map(entity -> new CoachAuthData(entity.getId(), entity.getHashedPassword()));
    }

}
