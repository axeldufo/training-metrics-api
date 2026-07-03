package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.DeleteWeeklyWellnessUseCase;
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
public class DeleteWeeklyWellnessUseCaseImpl implements DeleteWeeklyWellnessUseCase {

    private final AthleteRepository athleteRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;

    public DeleteWeeklyWellnessUseCaseImpl(AthleteRepository athleteRepository,
                                            WeeklyWellnessRepository weeklyWellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
    }

    @Override
    @Transactional
    @Observed(name = "use-case.delete-weekly-wellness")
    public void execute(long wellnessId, long athleteId, long coachId) {
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
        weeklyWellnessRepository.deleteById(wellnessId);
    }
}
