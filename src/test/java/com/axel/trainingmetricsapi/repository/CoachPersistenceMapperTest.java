package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Coach;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CoachPersistenceMapperTest {

    private final CoachPersistenceMapper coachPersistenceMapper = new CoachPersistenceMapper();

    @Test
    void domainToEntity_shouldMapAllFields() {
        Coach coach = Instancio.create(Coach.class);
        coach.setId(null); // id null before persistence

        CoachJpaEntity coachEntity = coachPersistenceMapper.domainToEntity(coach);

        assertCoachScalarFieldsMap(coach, coachEntity);
        assertThat(coachEntity.getId()).isNull();
    }

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
        assertThat(coach.getId()).isEqualTo(coachEntity.getId());
    }

}
