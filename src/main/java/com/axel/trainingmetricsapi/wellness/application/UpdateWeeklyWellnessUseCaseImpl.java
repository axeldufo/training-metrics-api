package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.UpdateWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyWellnessAlreadyExistsException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyWellnessNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UpdateWeeklyWellnessUseCaseImpl implements UpdateWeeklyWellnessUseCase {

    private final AthleteRepository athleteRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;

    public UpdateWeeklyWellnessUseCaseImpl(AthleteRepository athleteRepository,
                                            WeeklyWellnessRepository weeklyWellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
    }

    @Override
    @Transactional
    public WeeklyWellness execute(WeeklyWellness wellness, long coachId) {
        long athleteId = wellness.getAthleteId();
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        long wellnessId = wellness.getId();
        WeeklyWellness existing = weeklyWellnessRepository.findById(wellnessId)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(wellnessId));
        if (existing.getAthleteId() != wellness.getAthleteId()) {
            throw new WeeklyWellnessNotFoundException(wellnessId);
        }
        if (!wellness.getWeekStartDate().equals(existing.getWeekStartDate())
            && weeklyWellnessRepository.existsByAthleteIdAndWeekStartDate(athleteId, wellness.getWeekStartDate())) {
            throw new WeeklyWellnessAlreadyExistsException(athleteId, wellness.getWeekStartDate());
        }
        return weeklyWellnessRepository.save(wellness);
    }
}
