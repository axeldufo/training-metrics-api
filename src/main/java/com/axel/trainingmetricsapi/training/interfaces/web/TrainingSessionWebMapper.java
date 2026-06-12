package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionRequest;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionResponse;
import org.springframework.stereotype.Component;

@Component
public class TrainingSessionWebMapper {

    public TrainingSession requestToDomain(TrainingSessionRequest trainingSessionRequest, long athleteId) {
        return new TrainingSession(
            trainingSessionRequest.date(),
            trainingSessionRequest.sport(),
            trainingSessionRequest.rpe(),
            trainingSessionRequest.durationInMin(),
            trainingSessionRequest.targetZone(),
            athleteId
        );
    }

    public TrainingSessionResponse domainToResponse(TrainingSession trainingSession) {
        return new TrainingSessionResponse(
            trainingSession.getId(),
            trainingSession.getDate(),
            trainingSession.getSport(),
            trainingSession.getRpe(),
            trainingSession.getDurationInMin(),
            trainingSession.getTargetZone(),
            trainingSession.getAthleteId(),
            trainingSession.isAboveTargetZone(),
            trainingSession.isBelowTargetZone()
        );
    }

}
