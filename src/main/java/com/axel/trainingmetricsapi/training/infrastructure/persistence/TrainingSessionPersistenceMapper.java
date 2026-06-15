package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import org.springframework.stereotype.Component;

@Component
public class TrainingSessionPersistenceMapper {

    public TrainingSessionJpaEntity domainToEntity(TrainingSession trainingSession) {
        return new TrainingSessionJpaEntity(
            trainingSession.getId(),
            trainingSession.getDate(),
            trainingSession.getSport(),
            trainingSession.getRpe(),
            trainingSession.getDurationInMin(),
            trainingSession.getTargetZone(),
            trainingSession.getAthleteId()
        );
    }

    public TrainingSession entityToDomain(TrainingSessionJpaEntity trainingSessionJpaEntity) {
        TrainingSession trainingSession = new TrainingSession(
            trainingSessionJpaEntity.getDate(),
            trainingSessionJpaEntity.getSport(),
            trainingSessionJpaEntity.getRpe(),
            trainingSessionJpaEntity.getDurationInMin(),
            trainingSessionJpaEntity.getTargetZone(),
            trainingSessionJpaEntity.getAthleteId()
        );
        trainingSession.setId(trainingSessionJpaEntity.getId());
        return trainingSession;
    }
}
