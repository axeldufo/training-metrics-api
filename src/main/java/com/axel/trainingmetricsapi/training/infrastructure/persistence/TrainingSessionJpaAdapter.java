package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingSessionJpaAdapter implements TrainingSessionRepository {

    private final TrainingSessionJpaRepository trainingSessionRepository;
    private final TrainingSessionPersistenceMapper trainingSessionPersistenceMapper;

    public TrainingSessionJpaAdapter(TrainingSessionJpaRepository trainingSessionRepository,
                                     TrainingSessionPersistenceMapper trainingSessionPersistenceMapper) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingSessionPersistenceMapper = trainingSessionPersistenceMapper;
    }

    @Override
    public TrainingSession save(TrainingSession trainingSession) {
        TrainingSessionJpaEntity entityToPersist = trainingSessionPersistenceMapper.domainToEntity(trainingSession);
        TrainingSessionJpaEntity persistedEntity = trainingSessionRepository.save(entityToPersist);
        return trainingSessionPersistenceMapper.entityToDomain(persistedEntity);
    }

    @Override
    public Optional<TrainingSession> findById(long id) {
        return trainingSessionRepository.findById(id)
            .map(trainingSessionPersistenceMapper::entityToDomain);
    }

    @Override
    public List<TrainingSession> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to) {
        return trainingSessionRepository.findAllByAthleteIdAndDateBetween(athleteId, from, to)
            .stream()
            .map(trainingSessionPersistenceMapper::entityToDomain)
            .toList();
    }

    @Override
    public void deleteById(long id) {
        trainingSessionRepository.deleteById(id);
    }

    @Override
    public boolean existsById(long id) {
        return trainingSessionRepository.existsById(id);
    }
}
