package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AthleteServiceImpl implements AthleteService {

    private final AthleteRepository athleteRepository;

    public AthleteServiceImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    public List<Athlete> findAll() {
        return athleteRepository.findAll();
    }

    @Override
    public Athlete save(Athlete athlete) {
        return athleteRepository.save(athlete);
    }

    @Override
    public Athlete findById(Long athleteId) {
        return athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
    }

    @Override
    public Athlete update(Athlete athlete) {
        long athleteId = athlete.getId();
        athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        return athleteRepository.save(athlete);
    }
}
