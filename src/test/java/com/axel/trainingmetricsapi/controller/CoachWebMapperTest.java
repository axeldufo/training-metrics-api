package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.dto.request.CoachRequest;
import com.axel.trainingmetricsapi.dto.response.CoachResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CoachWebMapperTest {

    private final CoachWebMapper coachWebMapper = new CoachWebMapper();

    @Test
    void domainToResponse_shouldMapAllFields() {
        Coach coach = Instancio.create(Coach.class);

        CoachResponse coachResponse = coachWebMapper.domainToResponse(coach);

        assertThat(coach.getName()).isEqualTo(coachResponse.name());
        assertThat(coach.getId()).isEqualTo(coachResponse.id());
    }

    @Test
    void requestToDomain_shouldMapAllFields() {
        CoachRequest coachRequest = Instancio.create(CoachRequest.class);

        Coach coach = coachWebMapper.requestToDomain(coachRequest);

        assertThat(coach.getName()).isEqualTo(coachRequest.name());
    }

}
