package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.application.port.in.GetWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetWeeklyWellnessUseCaseImpl implements GetWeeklyWellnessUseCase {

    private final AthleteRepository athleteRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;

    public GetWeeklyWellnessUseCaseImpl(AthleteRepository athleteRepository,
                                         WeeklyWellnessRepository weeklyWellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
    }

    @Override
    public WeeklyWellness execute(long wellnessId, long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        WeeklyWellness wellness = weeklyWellnessRepository.findById(wellnessId)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(wellnessId));
        if (wellness.getAthleteId() != athleteId) {
            throw new WeeklyWellnessNotFoundException(wellnessId);
        }
        return wellness;
    }
}
