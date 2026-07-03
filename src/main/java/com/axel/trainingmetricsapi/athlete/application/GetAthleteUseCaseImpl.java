package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.application.port.in.GetAthleteUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAthleteUseCaseImpl implements GetAthleteUseCase {

    private final AthleteRepository athleteRepository;

    public GetAthleteUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-athlete")
    public Athlete execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return athlete;
    }
}
