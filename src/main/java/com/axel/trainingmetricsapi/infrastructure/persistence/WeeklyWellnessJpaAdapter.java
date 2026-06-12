package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class WeeklyWellnessJpaAdapter implements WeeklyWellnessRepository {

    private final WeeklyWellnessJpaRepository wellnessJpaRepository;
    private final WeeklyWellnessPersistenceMapper persistenceMapper;

    public WeeklyWellnessJpaAdapter(WeeklyWellnessJpaRepository wellnessJpaRepository,
                                    WeeklyWellnessPersistenceMapper persistenceMapper) {
        this.wellnessJpaRepository = wellnessJpaRepository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    public WeeklyWellness save(WeeklyWellness wellness) {
        WeeklyWellnessJpaEntity entityToPersist = persistenceMapper.domainToEntity(wellness);
        WeeklyWellnessJpaEntity persistedEntity = wellnessJpaRepository.save(entityToPersist);
        return persistenceMapper.entityToDomain(persistedEntity);
    }

    @Override
    public Optional<WeeklyWellness> findById(long id) {
        return wellnessJpaRepository.findById(id)
            .map(persistenceMapper::entityToDomain);
    }

    @Override
    public List<WeeklyWellness> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to) {
        return wellnessJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to)
            .stream()
            .map(persistenceMapper::entityToDomain)
            .toList();
    }

    @Override
    public Optional<WeeklyWellness> findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate) {
        return wellnessJpaRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
            .map(persistenceMapper::entityToDomain);
    }

    @Override
    public Optional<WeeklyWellness> findLatestByAthleteId(long athleteId) {
        return wellnessJpaRepository.findTopByAthleteIdOrderByWeekStartDateDesc(athleteId)
            .map(persistenceMapper::entityToDomain);
    }

    @Override
    public void deleteById(long id) {
        wellnessJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return wellnessJpaRepository.existsById(id);
    }

    @Override
    public boolean existsByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate) {
        return wellnessJpaRepository.existsByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
    }
}
