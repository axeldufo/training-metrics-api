package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetLatestWeeklyReportUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import org.springframework.stereotype.Service;
import io.micrometer.observation.annotation.Observed;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetLatestWeeklyReportUseCaseImpl implements GetLatestWeeklyReportUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;
    private final GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase;

    public GetLatestWeeklyReportUseCaseImpl(AthleteRepository athleteRepository,
                                             LoadReportRepository loadReportRepository,
                                             GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
        this.getWeeklyReportByWeekUseCase = getWeeklyReportByWeekUseCase;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-latest-weekly-report")
    public WeeklyReport execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        LoadReport latest = loadReportRepository.findLatestByAthleteId(athleteId)
            .orElseThrow(() -> new WeeklyReportNotFoundException(athleteId));
        return getWeeklyReportByWeekUseCase.execute(athleteId, coachId, latest.weekStartDate());
    }
}
