package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.UpdateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
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
public class UpdateTrainingSessionUseCaseImpl implements UpdateTrainingSessionUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionEventPort trainingSessionEventPort;

    public UpdateTrainingSessionUseCaseImpl(AthleteRepository athleteRepository,
                                             TrainingSessionRepository trainingSessionRepository,
                                             TrainingSessionEventPort trainingSessionEventPort) {
        this.athleteRepository = athleteRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingSessionEventPort = trainingSessionEventPort;
    }

    @Override
    @Transactional
    public TrainingSession execute(TrainingSession session, long coachId) {
        long athleteId = session.getAthleteId();
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        long sessionId = session.getId();
        TrainingSession existing = trainingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new TrainingSessionNotFoundException(sessionId));
        if (existing.getAthleteId() != session.getAthleteId()) {
            throw new TrainingSessionNotFoundException(sessionId);
        }
        TrainingSession updated = trainingSessionRepository.save(session);
        trainingSessionEventPort.sessionUpdated(updated.getAthleteId(), updated.getDate());
        return updated;
    }
}
