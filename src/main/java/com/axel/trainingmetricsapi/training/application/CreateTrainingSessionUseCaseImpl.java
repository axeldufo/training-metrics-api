package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.in.CreateTrainingSessionUseCase;
import com.axel.trainingmetricsapi.training.application.port.out.TrainingSessionEventPort;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
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
    @Observed(name = "use-case.create-training-session")
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
