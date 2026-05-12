package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TrainingSessionServiceImpl implements TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final AthleteRepository athleteRepository;

    public TrainingSessionServiceImpl(TrainingSessionRepository trainingSessionRepository,
                                      AthleteRepository athleteRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional
    public TrainingSession save(TrainingSession trainingSession) {
        long athleteId = trainingSession.getAthleteId();
        if (!athleteRepository.existsById(athleteId)) {
            throw new AthleteNotFoundException(athleteId);
        }
        return trainingSessionRepository.save(trainingSession);
    }

}
