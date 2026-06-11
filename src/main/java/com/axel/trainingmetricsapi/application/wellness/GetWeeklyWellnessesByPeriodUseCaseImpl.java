package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.application.port.in.GetWeeklyWellnessesByPeriodUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetWeeklyWellnessesByPeriodUseCaseImpl implements GetWeeklyWellnessesByPeriodUseCase {

    private final AthleteRepository athleteRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;

    public GetWeeklyWellnessesByPeriodUseCaseImpl(AthleteRepository athleteRepository,
                                                   WeeklyWellnessRepository weeklyWellnessRepository) {
        this.athleteRepository = athleteRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
    }

    @Override
    public List<WeeklyWellness> execute(long athleteId, long coachId, LocalDate from, LocalDate to) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return weeklyWellnessRepository.findByAthleteIdAndPeriod(athleteId, from, to);
    }
}
