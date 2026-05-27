package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
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

    @Override
    public PageResult<TrainingSession> findAllByAthleteId(long athleteId, int pageNumber, int pageSize) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new AthleteNotFoundException(athleteId);
        }
        return trainingSessionRepository.findAllByAthleteId(athleteId, pageNumber, pageSize);
    }

    @Override
    public TrainingSession findById(long id, long athleteId) {
        TrainingSession trainingSessionFound = trainingSessionRepository.findById(id)
            .orElseThrow(() -> new TrainingSessionNotFoundException(id));
        if (trainingSessionFound.getAthleteId() != athleteId) {
            // Return 404 instead of 403 to avoid revealing that the session belongs to another athlete
            throw new TrainingSessionNotFoundException(id);
        }
        return trainingSessionFound;
    }

    @Override
    @Transactional
    public TrainingSession update(TrainingSession trainingSession) {
        long id = trainingSession.getId();
        TrainingSession existingTrainingSession = trainingSessionRepository.findById(id)
            .orElseThrow(() -> new TrainingSessionNotFoundException(id));
        if (existingTrainingSession.getAthleteId() != trainingSession.getAthleteId()) {
            // Return 404 instead of 403 to avoid revealing that the session belongs to another athlete
            throw new TrainingSessionNotFoundException(id);
        }
        return trainingSessionRepository.save(trainingSession);
    }

    @Override
    public void deleteById(long id, long athleteId) {
        TrainingSession existingTrainingSession = trainingSessionRepository.findById(id)
            .orElseThrow(() -> new TrainingSessionNotFoundException(id));
        if (existingTrainingSession.getAthleteId() != athleteId) {
            // Return 404 instead of 403 to avoid revealing that the session belongs to another athlete
            throw new TrainingSessionNotFoundException(id);
        }
        trainingSessionRepository.deleteById(id);
    }

}
