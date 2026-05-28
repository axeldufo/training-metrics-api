package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.TrainingSessionNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainingSessionServiceImpl implements TrainingSessionService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final AthleteRepository athleteRepository;
    private final ApplicationEventPublisher eventPublisher;

    public TrainingSessionServiceImpl(TrainingSessionRepository trainingSessionRepository,
                                      AthleteRepository athleteRepository,
                                      ApplicationEventPublisher eventPublisher) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.athleteRepository = athleteRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public TrainingSession save(TrainingSession trainingSession) {
        long athleteId = trainingSession.getAthleteId();
        if (!athleteRepository.existsById(athleteId)) {
            throw new AthleteNotFoundException(athleteId);
        }
        TrainingSession saved = trainingSessionRepository.save(trainingSession);
        eventPublisher.publishEvent(new TrainingSessionCreatedEvent(saved.getAthleteId(), saved.getDate()));
        return saved;
    }

    @Override
    public List<TrainingSession> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to) {
        if (!athleteRepository.existsById(athleteId)) {
            throw new AthleteNotFoundException(athleteId);
        }
        return trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, from, to);
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
        TrainingSession updated = trainingSessionRepository.save(trainingSession);
        eventPublisher.publishEvent(new TrainingSessionUpdatedEvent(updated.getAthleteId(), updated.getDate()));
        return updated;
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
        eventPublisher.publishEvent(new TrainingSessionDeletedEvent(athleteId, existingTrainingSession.getDate()));
    }

}
