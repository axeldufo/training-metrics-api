package com.axel.trainingmetricsapi.application.athlete;

import com.axel.trainingmetricsapi.application.port.in.GetAthletesByCoachUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.PageResult;
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
