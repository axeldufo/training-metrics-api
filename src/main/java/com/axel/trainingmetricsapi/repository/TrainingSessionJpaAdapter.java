package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

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
    public PageResult<TrainingSession> findAllByAthleteId(long athleteId, int pageNumber, int pageSize) {
        Page<TrainingSessionJpaEntity> trainingSessionPage = trainingSessionRepository
            .findAllByAthleteId(athleteId, PageRequest.of(pageNumber, pageSize));

        List<TrainingSession> trainingSessions = trainingSessionPage.stream()
            .map(trainingSessionPersistenceMapper::entityToDomain)
            .toList();

        return new PageResult<>(
            trainingSessions,
            trainingSessionPage.getTotalElements(),
            trainingSessionPage.getNumber(),
            trainingSessionPage.getSize());
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
