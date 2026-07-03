package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.application.port.in.GetAthletesByCoachUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.shared.domain.PageResult;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAthletesByCoachUseCaseImpl implements GetAthletesByCoachUseCase {

    private final AthleteRepository athleteRepository;

    public GetAthletesByCoachUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-athletes-by-coach")
    public PageResult<Athlete> execute(long coachId, int pageNumber, int pageSize) {
        return athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize);
    }
}
