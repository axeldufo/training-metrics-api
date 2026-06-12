package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.CoachResponse;
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

}
