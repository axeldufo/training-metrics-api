package com.axel.trainingmetricsapi.mapper;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import com.axel.trainingmetricsapi.repository.AthleteJpaEntity;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteMapperTest {

    private final AthleteMapper athleteMapper = new AthleteMapper();

    @Test
    void domainToEntity_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);
        athlete.setId(null); // id null before persistence

        AthleteJpaEntity athleteEntity = athleteMapper.domainToEntity(athlete);

        assertAthleteFieldsMap(athlete, athleteEntity);
        assertThat(athleteEntity.getId()).isNull();
    }

    @Test
    void domainToEntity_shouldHandleNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, null);

        AthleteJpaEntity athleteEntity = athleteMapper.domainToEntity(athlete);

        assertThat(athleteEntity.getBirthDate()).isNull();
        assertThat(athleteEntity.getWeightInKg()).isNull();
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        AthleteJpaEntity athleteEntity = Instancio.create(AthleteJpaEntity.class);
        athleteEntity.setId(42L);

        Athlete athlete = athleteMapper.entityToDomain(athleteEntity);

        assertAthleteFieldsMap(athlete, athleteEntity);
        assertThat(athlete.getId()).isNotNull();
    }

    @Test
    void entityToDomain_shouldHandleNullableFields() {
        AthleteJpaEntity athleteEntity = new AthleteJpaEntity(2L, "Jean", "Dupont", null, Sport.TRIATHLON, null);

        Athlete athlete = athleteMapper.entityToDomain(athleteEntity);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
    }

    @Test
    void domainToResponse_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);

        AthleteResponse athleteResponse = athleteMapper.domainToResponse(athlete);

        assertAthleteFieldsMap(athlete, athleteResponse);
    }

    @Test
    void domainToResponse_shouldMapNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, null);
        athlete.setId(4L); // persisted object returned to client

        AthleteResponse athleteResponse = athleteMapper.domainToResponse(athlete);

        assertThat(athleteResponse.birthDate()).isNull();
        assertThat(athleteResponse.weightInKg()).isNull();
    }

    @Test
    void requestToDomain_shouldMapAllFields() {
        AthleteRequest athleteRequest = Instancio.create(AthleteRequest.class);

        Athlete athlete = athleteMapper.requestToDomain(athleteRequest);

        assertAthleteFieldsMap(athlete, athleteRequest);
    }

    @Test
    void requestToDomain_shouldMapNullableFields() {
        AthleteRequest athleteRequest = new AthleteRequest("Jean", "Dupont", null, Sport.TRIATHLON, null);

        Athlete athlete = athleteMapper.requestToDomain(athleteRequest);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
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

    private void assertAthleteFieldsMap(Athlete athlete, AthleteRequest athleteRequest) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteRequest.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteRequest.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteRequest.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteRequest.sport());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteRequest.weightInKg());
    }

}
