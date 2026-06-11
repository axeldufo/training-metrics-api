package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.GetTrainingSessionUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetTrainingSessionUseCaseImpl implements GetTrainingSessionUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    public GetTrainingSessionUseCaseImpl(AthleteRepository athleteRepository,
                                         TrainingSessionRepository trainingSessionRepository) {
        this.athleteRepository = athleteRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    public TrainingSession execute(long sessionId, long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        TrainingSession session = trainingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new TrainingSessionNotFoundException(sessionId));
        if (session.getAthleteId() != athleteId) {
            throw new TrainingSessionNotFoundException(sessionId);
        }
        return session;
    }
}
