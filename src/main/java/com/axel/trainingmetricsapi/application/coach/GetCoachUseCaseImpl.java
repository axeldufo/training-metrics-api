package com.axel.trainingmetricsapi.application.coach;

import com.axel.trainingmetricsapi.application.port.in.GetCoachUseCase;
import com.axel.trainingmetricsapi.domain.Coach;
import com.axel.trainingmetricsapi.domain.CoachRepository;
import com.axel.trainingmetricsapi.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetCoachUseCaseImpl implements GetCoachUseCase {

    private final CoachRepository coachRepository;

    public GetCoachUseCaseImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    public Coach execute(long coachId) {
        return coachRepository.findById(coachId)
            .orElseThrow(() -> new CoachNotFoundException(coachId));
    }
}
