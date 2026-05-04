package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.Sport;
import com.axel.trainingmetricsapi.dto.request.AthleteRequest;
import com.axel.trainingmetricsapi.dto.response.AthleteResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteWebMapperTest {

    private final AthleteWebMapper athleteWebMapper = new AthleteWebMapper();

    @Test
    void domainToResponse_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);

        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athlete);

        assertAthleteFieldsMap(athlete, athleteResponse);
    }

    @Test
    void domainToResponse_shouldMapNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, null, null);
        athlete.setId(4L); // persisted object returned to client

        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athlete);

        assertThat(athleteResponse.birthDate()).isNull();
        assertThat(athleteResponse.coachId()).isNull();
        assertThat(athleteResponse.weightInKg()).isNull();
    }

    @Test
    void requestToDomain_shouldMapAllFields() {
        AthleteRequest athleteRequest = Instancio.create(AthleteRequest.class);

        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest);

        assertAthleteFieldsMap(athlete, athleteRequest);
    }

    @Test
    void requestToDomain_shouldMapNullableFields() {
        AthleteRequest athleteRequest = new AthleteRequest("Jean", "Dupont", null, Sport.TRIATHLON, null, null);

        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getCoachId()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
    }

    private void assertAthleteFieldsMap(Athlete athlete, AthleteResponse athleteResponse) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteResponse.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteResponse.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteResponse.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteResponse.sport());
        assertThat(athlete.getCoachId()).isEqualTo(athleteResponse.coachId());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteResponse.weightInKg());
        assertThat(athlete.getId()).isEqualTo(athleteResponse.id());
    }

    private void assertAthleteFieldsMap(Athlete athlete, AthleteRequest athleteRequest) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteRequest.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteRequest.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteRequest.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteRequest.sport());
        assertThat(athlete.getCoachId()).isEqualTo(athleteRequest.coachId());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteRequest.weightInKg());
    }
}
