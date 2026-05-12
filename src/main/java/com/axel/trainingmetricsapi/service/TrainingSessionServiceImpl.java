package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Override
    public List<TrainingSession> findAllByAthleteId(long athleteId) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new AthleteNotFoundException(athleteId);
        }
        return trainingSessionRepository.findAllByAthleteId(athleteId);
    }

    @Override
    public TrainingSession findById(long id) {
        return trainingSessionRepository.findById(id)
            .orElseThrow(() -> new TrainingSessionNotFoundException(id));
    }

    @Override
    @Transactional
    public TrainingSession update(TrainingSession trainingSession) {
        long id = trainingSession.getId();
        if (!trainingSessionRepository.existsById(id)) {
            throw new TrainingSessionNotFoundException(id);
        }
        return trainingSessionRepository.save(trainingSession);
    }

}
