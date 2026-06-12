package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.infrastructure.persistence.AthleteJpaEntity;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import org.springframework.stereotype.Component;

@Component
public class TrainingSessionPersistenceMapper {

    public TrainingSessionJpaEntity domainToEntity(TrainingSession trainingSession) {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(); // Phantom entity
        athleteEntity.setId(trainingSession.getAthleteId());     // Hibernate only needs the id to persist the FK
        return new TrainingSessionJpaEntity(
            trainingSession.getId(),
            trainingSession.getDate(),
            trainingSession.getSport(),
            trainingSession.getRpe(),
            trainingSession.getDurationInMin(),
            trainingSession.getTargetZone(),
            athleteEntity
        );
    }

    public TrainingSession entityToDomain(TrainingSessionJpaEntity trainingSessionJpaEntity) {
        TrainingSession trainingSession = new TrainingSession(
            trainingSessionJpaEntity.getDate(),
            trainingSessionJpaEntity.getSport(),
            trainingSessionJpaEntity.getRpe(),
            trainingSessionJpaEntity.getDurationInMin(),
            trainingSessionJpaEntity.getTargetZone(),
            trainingSessionJpaEntity.getAthlete().getId()   // Athlete not nullable in TrainingSessionEntity
        );
        trainingSession.setId(trainingSessionJpaEntity.getId());
        return trainingSession;
    }
}
