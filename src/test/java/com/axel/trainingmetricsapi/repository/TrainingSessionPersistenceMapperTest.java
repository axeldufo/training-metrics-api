package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.TrainingSession;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TrainingSessionPersistenceMapperTest {

    private final TrainingSessionPersistenceMapper trainingSessionPersistenceMapper =
        new TrainingSessionPersistenceMapper();

    @Test
    void domainToEntity_shouldMapAllFields() {
        TrainingSession trainingSession = Instancio.of(TrainingSession.class)
            .generate(field(TrainingSession::getRpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSession::getDurationInMin), gen -> gen.ints().min(1))
            .create();
        trainingSession.setId(null); // id null before persistence

        TrainingSessionJpaEntity trainingSessionEntity =
            trainingSessionPersistenceMapper.domainToEntity(trainingSession);

        assertTrainingSessionScalarFieldsMap(trainingSession, trainingSessionEntity);
        assertThat(trainingSession.getAthleteId()).isEqualTo(trainingSessionEntity.getAthlete().getId());
        assertThat(trainingSessionEntity.getId()).isNull();
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        TrainingSessionJpaEntity trainingSessionEntity = Instancio.of(TrainingSessionJpaEntity.class)
            .generate(field(TrainingSessionJpaEntity::getRpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSessionJpaEntity::getDurationInMin), gen -> gen.ints().min(1))
            .create();
        trainingSessionEntity.setId(42L);

        TrainingSession trainingSession = trainingSessionPersistenceMapper.entityToDomain(trainingSessionEntity);

        assertTrainingSessionScalarFieldsMap(trainingSession, trainingSessionEntity);
        assertThat(trainingSession.getAthleteId()).isEqualTo(trainingSessionEntity.getAthlete().getId());
        assertThat(trainingSession.getId()).isNotNull();
    }

    private void assertTrainingSessionScalarFieldsMap(TrainingSession trainingSession,
                                                      TrainingSessionJpaEntity trainingSessionEntity) {
        assertThat(trainingSession.getDate()).isEqualTo(trainingSessionEntity.getDate());
        assertThat(trainingSession.getSport()).isEqualTo(trainingSessionEntity.getSport());
        assertThat(trainingSession.getRpe()).isEqualTo(trainingSessionEntity.getRpe());
        assertThat(trainingSession.getDurationInMin()).isEqualTo(trainingSessionEntity.getDurationInMin());
        assertThat(trainingSession.getTargetZone()).isEqualTo(trainingSessionEntity.getTargetZone());
        assertThat(trainingSession.getId()).isEqualTo(trainingSessionEntity.getId());
    }

}
