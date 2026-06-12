package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CoachJpaAdapter implements CoachRepository {

    private final CoachJpaRepository coachJpaRepository;
    private final CoachPersistenceMapper coachPersistenceMapper;

    public CoachJpaAdapter(CoachJpaRepository coachJpaRepository, CoachPersistenceMapper coachPersistenceMapper) {
        this.coachJpaRepository = coachJpaRepository;
        this.coachPersistenceMapper = coachPersistenceMapper;
    }

    @Override
    public Optional<Coach> findById(long id) {
        return coachJpaRepository.findById(id)
            .map(coachPersistenceMapper::entityToDomain);
    }

    @Override
    public void deleteById(long id) {
        coachJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return coachJpaRepository.existsById(id);
    }

    @Override
    public void updateName(long id, String name) {
        coachJpaRepository.updateName(id, name);
    }
}
