package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AthleteJpaAdapter implements AthleteRepository {

    private final AthleteJpaRepository athleteJpaRepository;
    private final AthletePersistenceMapper athletePersistenceMapper;

    public AthleteJpaAdapter(AthleteJpaRepository athleteJpaRepository, AthletePersistenceMapper athletePersistenceMapper) {
        this.athleteJpaRepository = athleteJpaRepository;
        this.athletePersistenceMapper = athletePersistenceMapper;
    }

    @Override
    public Athlete save(Athlete athlete) {
        AthleteJpaEntity entityToPersist = athletePersistenceMapper.domainToEntity(athlete);
        AthleteJpaEntity persistedEntity = athleteJpaRepository.save(entityToPersist);
        return athletePersistenceMapper.entityToDomain(persistedEntity);
    }

    @Override
    public Optional<Athlete> findById(Long id) {
        return athleteJpaRepository.findById(id).map(athletePersistenceMapper::entityToDomain);
    }

    @Override
    public List<Athlete> findAll() {
        return athleteJpaRepository.findAll().stream()
            .map(athletePersistenceMapper::entityToDomain)
            .toList();
    }

    @Override
    public void deleteById(Long id) {
        athleteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long athleteId) {
        return athleteJpaRepository.existsById(athleteId);
    }
}
