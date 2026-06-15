package com.axel.trainingmetricsapi.athlete.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.shared.domain.PageResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public Optional<Athlete> findById(long id) {
        return athleteJpaRepository.findById(id)
            .map(athletePersistenceMapper::entityToDomain);
    }

    @Override
    public PageResult<Athlete> findAllByCoachId(long coachId, int pageNumber, int pageSize) {
        Page<AthleteJpaEntity> athletePage = athleteJpaRepository
            .findAllByCoachId(coachId, PageRequest.of(pageNumber, pageSize));

        List<Athlete> athletes = athletePage.stream()
            .map(athletePersistenceMapper::entityToDomain)
            .toList();

        return new PageResult<>(
            athletes,
            athletePage.getTotalElements(),
            athletePage.getNumber(),
            athletePage.getSize());
    }

    @Override
    public void deleteById(long id) {
        athleteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return athleteJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByCoachId(long coachId) {
        return athleteJpaRepository.existsByCoachId(coachId);
    }
}
