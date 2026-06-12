package com.axel.trainingmetricsapi.application.athlete;

import com.axel.trainingmetricsapi.application.port.in.CreateAthleteUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CreateAthleteUseCaseImpl implements CreateAthleteUseCase {

    private final AthleteRepository athleteRepository;

    public CreateAthleteUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional
    public Athlete execute(Athlete athlete) {
        return athleteRepository.save(athlete);
    }
}
