package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    public Coach save(Coach coach) {
        CoachJpaEntity entityToPersist = coachPersistenceMapper.domainToEntity(coach);
        CoachJpaEntity persistedEntity = coachJpaRepository.save(entityToPersist);
        return coachPersistenceMapper.entityToDomain(persistedEntity);
    }

    @Override
    public Optional<Coach> findById(long id) {
        return coachJpaRepository.findById(id)
            .map(coachPersistenceMapper::entityToDomain);
    }

    @Override
    public List<Coach> findAll() {
        return coachJpaRepository.findAll().stream()
            .map(coachPersistenceMapper::entityToDomain)
            .toList();
    }

    @Override
    public void deleteById(long id) {
        coachJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return coachJpaRepository.existsById(id);
    }
}
