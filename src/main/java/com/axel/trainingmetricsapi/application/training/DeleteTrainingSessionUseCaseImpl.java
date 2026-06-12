package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.DeleteTrainingSessionUseCase;
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
public class DeleteTrainingSessionUseCaseImpl implements DeleteTrainingSessionUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionEventPort trainingSessionEventPort;

    public DeleteTrainingSessionUseCaseImpl(AthleteRepository athleteRepository,
                                             TrainingSessionRepository trainingSessionRepository,
                                             TrainingSessionEventPort trainingSessionEventPort) {
        this.athleteRepository = athleteRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.trainingSessionEventPort = trainingSessionEventPort;
    }

    @Override
    @Transactional
    public void execute(long sessionId, long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        TrainingSession existing = trainingSessionRepository.findById(sessionId)
            .orElseThrow(() -> new TrainingSessionNotFoundException(sessionId));
        if (existing.getAthleteId() != athleteId) {
            throw new TrainingSessionNotFoundException(sessionId);
        }
        trainingSessionRepository.deleteById(sessionId);
        trainingSessionEventPort.sessionDeleted(athleteId, existing.getDate());
    }
}
