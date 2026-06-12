package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.interfaces.web.dto.request.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.WeeklyWellnessResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class WeeklyWellnessWebMapperTest {

    private final WeeklyWellnessWebMapper mapper = new WeeklyWellnessWebMapper();

    @Test
    void requestToDomain_shouldMapAllFields() {
        WeeklyWellnessRequest request = aValidRequest();
        long athleteId = 7L;

        WeeklyWellness wellness = mapper.requestToDomain(request, athleteId);

        assertThat(wellness.getAthleteId()).isEqualTo(athleteId);
        assertThat(wellness.getWeekStartDate()).isEqualTo(request.weekStartDate());
        assertThat(wellness.getPerceivedDifficulty()).isEqualTo(request.perceivedDifficulty());
        assertThat(wellness.getPerceivedFatigue()).isEqualTo(request.perceivedFatigue());
        assertThat(wellness.getMotivation()).isEqualTo(request.motivation());
    }

    @Test
    void domainToResponse_shouldMapAllFields() {
        WeeklyWellness wellness = aValidWellness();
        wellness.setId(99L);

        WeeklyWellnessResponse response = mapper.domainToResponse(wellness);

        assertThat(response.id()).isEqualTo(wellness.getId());
        assertThat(response.athleteId()).isEqualTo(wellness.getAthleteId());
        assertThat(response.weekStartDate()).isEqualTo(wellness.getWeekStartDate());
        assertThat(response.perceivedDifficulty()).isEqualTo(wellness.getPerceivedDifficulty());
        assertThat(response.perceivedFatigue()).isEqualTo(wellness.getPerceivedFatigue());
        assertThat(response.motivation()).isEqualTo(wellness.getMotivation());
    }

    private WeeklyWellnessRequest aValidRequest() {
        return Instancio.of(WeeklyWellnessRequest.class)
            .set(field(WeeklyWellnessRequest::weekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellnessRequest::perceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessRequest::perceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessRequest::motivation), gen -> gen.ints().range(1, 5))
            .create();
    }

    private WeeklyWellness aValidWellness() {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }
}
