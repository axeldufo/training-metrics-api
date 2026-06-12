package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionRequest;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TrainingSessionWebMapperTest {

    private final TrainingSessionWebMapper trainingSessionWebMapper = new TrainingSessionWebMapper();

    @Test
    void requestToDomain_shouldMapAllFields() {
        TrainingSessionRequest trainingSessionRequest = Instancio.of(TrainingSessionRequest.class)
            .generate(field(TrainingSessionRequest::rpe), gen -> gen.ints().range(1, 10))
            .generate(field(TrainingSessionRequest::durationInMin), gen -> gen.ints().min(1))
            .create();

        long athleteId = 4L;
        TrainingSession trainingSession = trainingSessionWebMapper.requestToDomain(trainingSessionRequest, athleteId);

        assertSessionMapsToRequest(trainingSession, trainingSessionRequest);
        assertThat(trainingSession.getAthleteId()).isEqualTo(athleteId);
    }

    @Test
    void domainToResponse_shouldMapAllFields() {
        TrainingSession trainingSession = Instancio.create(TrainingSession.class);

        TrainingSessionResponse trainingSessionResponse = trainingSessionWebMapper.domainToResponse(trainingSession);

        assertResponseMapsToSession(trainingSession, trainingSessionResponse);
    }

    private void assertSessionMapsToRequest(TrainingSession trainingSession,
                                                TrainingSessionRequest trainingSessionRequest) {
        assertThat(trainingSession.getDate()).isEqualTo(trainingSessionRequest.date());
        assertThat(trainingSession.getSport()).isEqualTo(trainingSessionRequest.sport());
        assertThat(trainingSession.getRpe()).isEqualTo(trainingSessionRequest.rpe());
        assertThat(trainingSession.getDurationInMin()).isEqualTo(trainingSessionRequest.durationInMin());
        assertThat(trainingSession.getTargetZone()).isEqualTo(trainingSessionRequest.targetZone());
    }

    private void assertResponseMapsToSession(TrainingSession trainingSession,
                                                TrainingSessionResponse trainingSessionResponse) {
        assertThat(trainingSession.getDate()).isEqualTo(trainingSessionResponse.date());
        assertThat(trainingSession.getSport()).isEqualTo(trainingSessionResponse.sport());
        assertThat(trainingSession.getRpe()).isEqualTo(trainingSessionResponse.rpe());
        assertThat(trainingSession.getDurationInMin()).isEqualTo(trainingSessionResponse.durationInMin());
        assertThat(trainingSession.getTargetZone()).isEqualTo(trainingSessionResponse.targetZone());
        assertThat(trainingSession.getAthleteId()).isEqualTo(trainingSessionResponse.athleteId());
        assertThat(trainingSession.isAboveTargetZone()).isEqualTo(trainingSessionResponse.aboveTargetAlert());
        assertThat(trainingSession.isBelowTargetZone()).isEqualTo(trainingSessionResponse.belowTargetAlert());
        assertThat(trainingSession.getId()).isEqualTo(trainingSessionResponse.id());
    }

}
