package com.axel.trainingmetricsapi.athlete.interfaces.web;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.shared.domain.Sport;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteRequest;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AthleteWebMapperTest {

    private final AthleteWebMapper athleteWebMapper = new AthleteWebMapper();

    @Test
    void requestToDomain_shouldMapAllFields() {
        AthleteRequest athleteRequest = Instancio.create(AthleteRequest.class);
        long coachId = 4L;

        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest, coachId);

        assertAthleteDomainMapsAthleteRequest(athlete, athleteRequest);
        assertThat(athlete.getCoachId()).isEqualTo(coachId);
    }

    @Test
    void requestToDomain_shouldMapNullableFields() {
        AthleteRequest athleteRequest = new AthleteRequest("Jean", "Dupont", null, Sport.TRIATHLON, null);

        Athlete athlete = athleteWebMapper.requestToDomain(athleteRequest, 4L);

        assertThat(athlete.getBirthDate()).isNull();
        assertThat(athlete.getWeightInKg()).isNull();
    }

    @Test
    void domainToResponse_shouldMapAllFields() {
        Athlete athlete = Instancio.create(Athlete.class);

        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athlete);

        assertAthleteResponseMapsAthleteDomain(athlete, athleteResponse);
    }

    @Test
    void domainToResponse_shouldMapNullableFields() {
        Athlete athlete = new Athlete("Jean", "Dupont", null, Sport.TRIATHLON, 2L, null);
        athlete.setId(4L); // persisted object returned to client

        AthleteResponse athleteResponse = athleteWebMapper.domainToResponse(athlete);

        assertThat(athleteResponse.birthDate()).isNull();
        assertThat(athleteResponse.weightInKg()).isNull();
    }

    private void assertAthleteDomainMapsAthleteRequest(Athlete athlete, AthleteRequest athleteRequest) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteRequest.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteRequest.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteRequest.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteRequest.sport());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteRequest.weightInKg());
    }

    private void assertAthleteResponseMapsAthleteDomain(Athlete athlete, AthleteResponse athleteResponse) {
        assertThat(athlete.getFirstName()).isEqualTo(athleteResponse.firstName());
        assertThat(athlete.getLastName()).isEqualTo(athleteResponse.lastName());
        assertThat(athlete.getBirthDate()).isEqualTo(athleteResponse.birthDate());
        assertThat(athlete.getSport()).isEqualTo(athleteResponse.sport());
        assertThat(athlete.getCoachId()).isEqualTo(athleteResponse.coachId());
        assertThat(athlete.getWeightInKg()).isEqualTo(athleteResponse.weightInKg());
        assertThat(athlete.getId()).isEqualTo(athleteResponse.id());
    }
}
