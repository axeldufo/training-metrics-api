package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.application.port.in.GetAthletesByCoachUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.shared.domain.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetAthletesByCoachUseCaseImpl implements GetAthletesByCoachUseCase {

    private final AthleteRepository athleteRepository;

    public GetAthletesByCoachUseCaseImpl(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Override
    public PageResult<Athlete> execute(long coachId, int pageNumber, int pageSize) {
        return athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize);
    }
}
