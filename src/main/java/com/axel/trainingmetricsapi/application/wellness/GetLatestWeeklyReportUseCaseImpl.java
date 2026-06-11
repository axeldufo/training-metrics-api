package com.axel.trainingmetricsapi.application.wellness;

import com.axel.trainingmetricsapi.application.port.in.GetLatestWeeklyReportUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyReportNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
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
