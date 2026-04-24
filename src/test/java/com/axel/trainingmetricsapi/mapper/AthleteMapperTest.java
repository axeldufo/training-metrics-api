package com.axel.trainingmetricsapi.mapper;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.repository.AthleteJpaEntity;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteMapperTest {

    private final AthleteMapper athleteMapper = new AthleteMapper();

    @Test
    void toEntity_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);
        athlete.setId(null); // id null before persistence

        AthleteJpaEntity athleteEntity = athleteMapper.toEntity(athlete);

        assertAthleteFieldsMap(athlete, athleteEntity);
        assertThat(athleteEntity.getId()).isNull();
    }

    @Test
    void toEntity_shouldHandleNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, null);

        AthleteJpaEntity athleteEntity = athleteMapper.toEntity(athlete);

        assertThat(athleteEntity.getBirthDate()).isNull();
        assertThat(athleteEntity.getWeightInKg()).isNull();
    }

    @Test
    void toDomain_shouldMapAllFields() {
        AthleteJpaEntity athleteEntity = Instancio.create(AthleteJpaEntity.class);
        athleteEntity.setId(42L);

        Athlete athlete = athleteMapper.toDomain(athleteEntity);

        assertAthleteFieldsMap(athlete, athleteEntity);
        assertThat(athlete.getId()).isNotNull();
    }

    @Test
    void toDomain_shouldHandleNullableFields() {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(2L, "Jean", "Dupont", null, Sport.TRIATHLON, null);

        Athlete athlete = athleteMapper.toDomain(athleteEntity);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);

        AthleteResponse athleteResponse = athleteMapper.toResponse(athlete);

        assertAthleteFieldsMap(athlete, athleteResponse);

    }

    @Test
    void toResponse_shouldMapNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, null);
        athlete.setId(4L); // persisted object returned to client

        AthleteResponse athleteResponse = athleteMapper.toResponse(athlete);

        assertThat(athleteResponse.birthDate()).isNull();
        assertThat(athleteResponse.weightInKg()).isNull();
    }

    private void assertAthleteFieldsMap(Athlete athlete, AthleteJpaEntity athleteEntity) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteEntity.getFirstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteEntity.getLastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteEntity.getBirthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteEntity.getSport());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteEntity.getWeightInKg());
        assertThat(athlete.getId()).isEqualTo(athleteEntity.getId());
    }

    private void assertAthleteFieldsMap(Athlete athlete, AthleteResponse athleteResponse) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteResponse.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteResponse.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteResponse.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteResponse.sport());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteResponse.weightInKg());
        assertThat(athlete.getId()).isEqualTo(athleteResponse.id());
    }

}
