package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.CreateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CreateTrainingSessionUseCaseImpl implements CreateTrainingSessionUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final TrainingSessionEventPort trainingSessionEventPort;

    public CreateTrainingSessionUseCaseImpl(AthleteRepository athleteRepository,
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
        TrainingSession saved = trainingSessionRepository.save(session);
        trainingSessionEventPort.sessionCreated(saved.getAthleteId(), saved.getDate());
        return saved;
    }
}
