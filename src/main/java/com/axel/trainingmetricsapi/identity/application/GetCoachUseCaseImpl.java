package com.axel.trainingmetricsapi.identity.application;

import com.axel.trainingmetricsapi.identity.application.port.in.GetCoachUseCase;
import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.domain.CoachRepository;
import com.axel.trainingmetricsapi.identity.domain.exception.CoachNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCoachUseCaseImpl implements GetCoachUseCase {

    private final CoachRepository coachRepository;

    public GetCoachUseCaseImpl(CoachRepository coachRepository) {
        this.coachRepository = coachRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-coach")
    public Coach execute(long coachId) {
        return coachRepository.findById(coachId)
            .orElseThrow(() -> new CoachNotFoundException(coachId));
    }
}
