package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.GetTrainingSessionsByPeriodUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetTrainingSessionsByPeriodUseCaseImpl implements GetTrainingSessionsByPeriodUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;

    public GetTrainingSessionsByPeriodUseCaseImpl(AthleteRepository athleteRepository,
                                                   TrainingSessionRepository trainingSessionRepository) {
        this.athleteRepository = athleteRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    @Override
    public List<TrainingSession> execute(long athleteId, long coachId, LocalDate from, LocalDate to) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, from, to);
    }
}
