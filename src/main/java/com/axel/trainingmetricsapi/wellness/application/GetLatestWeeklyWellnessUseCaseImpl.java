package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetLatestWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyWellnessNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLatestWeeklyWellnessUseCaseImpl implements GetLatestWeeklyWellnessUseCase {

    private final AthleteRepository athleteRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;

    public GetLatestWeeklyWellnessUseCaseImpl(AthleteRepository athleteRepository,
                                               WeeklyWellnessRepository weeklyWellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-latest-weekly-wellness")
    public WeeklyWellness execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return weeklyWellnessRepository.findLatestByAthleteId(athleteId)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(athleteId));
    }
}
