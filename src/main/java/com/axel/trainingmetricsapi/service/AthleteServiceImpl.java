package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.PageResult;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AthleteServiceImpl implements AthleteService {

    private final AthleteRepository athleteRepository;

    public AthleteServiceImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    public PageResult<Athlete> findAllByCoachId(long coachId, int pageNumber, int pageSize) {
        return athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize);
    }

    @Override
    @Transactional
    public Athlete save(Athlete athlete) {
        return athleteRepository.save(athlete);
    }

    @Override
    public Athlete findById(long athleteId, long coachId) {
        Athlete athleteFound = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (!athleteFound.getCoachId().equals(coachId)) {
            // Return 404 instead of 403 to avoid revealing that the athlete belongs to another coach
            throw new AthleteNotFoundException(athleteId);
        }
        return athleteFound;
    }

    @Override
    @Transactional
    public Athlete update(Athlete athlete) {
        long athleteId = athlete.getId();
        Athlete existingAthlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (!existingAthlete.getCoachId().equals(athlete.getCoachId())) {
            // Return 404 instead of 403 to avoid revealing that the athlete belongs to another coach
            throw new AthleteNotFoundException(athleteId);
        }
        return athleteRepository.save(athlete);
    }

    @Override
    @Transactional
    public void deleteById(long athleteId, long coachId) {
        Athlete existingAthlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (!existingAthlete.getCoachId().equals(coachId)) {
            // Return 404 instead of 403 to avoid revealing that the athlete belongs to another coach
            throw new AthleteNotFoundException(athleteId);
        }
        athleteRepository.deleteById(athleteId);
    }
}
