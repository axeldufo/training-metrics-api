package com.axel.trainingmetricsapi.athlete.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthletePersistenceMapperTest {

    private final AthletePersistenceMapper athletePersistenceMapper = new AthletePersistenceMapper();

    @Test
    void domainToEntity_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);
        athlete.setId(null); // id null before persistence

        AthleteJpaEntity athleteEntity = athletePersistenceMapper.domainToEntity(athlete);

        assertAthleteScalarFieldsMap(athlete, athleteEntity);
        assertThat(athlete.getCoachId()).isEqualTo(athleteEntity.getCoachId());
        assertThat(athleteEntity.getId()).isNull();
    }

    @Test
    void domainToEntity_shouldHandleNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, 4L, null);

        AthleteJpaEntity athleteEntity = athletePersistenceMapper.domainToEntity(athlete);

        assertThat(athleteEntity.getBirthDate()).isNull();
        assertThat(athleteEntity.getWeightInKg()).isNull();
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        AthleteJpaEntity athleteEntity = Instancio.create(AthleteJpaEntity.class);
        athleteEntity.setId(42L);

        Athlete athlete = athletePersistenceMapper.entityToDomain(athleteEntity);

        assertAthleteScalarFieldsMap(athlete, athleteEntity);
        assertThat(athlete.getCoachId()).isEqualTo(athleteEntity.getCoachId());
        assertThat(athlete.getId()).isNotNull();
    }

    @Test
    void entityToDomain_shouldHandleNullableFields() {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(2L, "Jean", "Dupont", null, Sport.TRIATHLON, 5L, null);

        Athlete athlete = athletePersistenceMapper.entityToDomain(athleteEntity);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
    }

    private void assertAthleteScalarFieldsMap(Athlete athlete, AthleteJpaEntity athleteEntity) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteEntity.getFirstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteEntity.getLastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteEntity.getBirthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteEntity.getSport());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteEntity.getWeightInKg());
        assertThat(athlete.getId()).isEqualTo(athleteEntity.getId());
    }

}
