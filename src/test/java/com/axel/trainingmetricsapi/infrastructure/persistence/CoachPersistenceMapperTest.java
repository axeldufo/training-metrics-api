package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.Coach;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoachPersistenceMapperTest {

    private final CoachPersistenceMapper coachPersistenceMapper = new CoachPersistenceMapper();

    @Test
    void entityToDomain_shouldMapAllFields() {
        CoachJpaEntity coachEntity = Instancio.create(CoachJpaEntity.class);
        coachEntity.setId(42L);

        Coach coach = coachPersistenceMapper.entityToDomain(coachEntity);

        assertCoachScalarFieldsMap(coach, coachEntity);
        assertThat(coach.getId()).isNotNull();
    }

    private void assertCoachScalarFieldsMap(Coach coach, CoachJpaEntity coachEntity) {
        assertThat(coach.getName()).isEqualTo(coachEntity.getName());
        assertThat(coach.getEmail()).isEqualTo(coachEntity.getEmail());
        assertThat(coach.getId()).isEqualTo(coachEntity.getId());
    }

}
