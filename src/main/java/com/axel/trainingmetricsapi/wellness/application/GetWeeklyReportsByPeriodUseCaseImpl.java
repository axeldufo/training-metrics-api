package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportsByPeriodUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class GetWeeklyReportsByPeriodUseCaseImpl implements GetWeeklyReportsByPeriodUseCase {

    private final AthleteRepository athleteRepository;
    private final GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase;

    public GetWeeklyReportsByPeriodUseCaseImpl(AthleteRepository athleteRepository,
                                                GetWeeklyReportByWeekUseCase getWeeklyReportByWeekUseCase) {
        this.athleteRepository = athleteRepository;
        this.getWeeklyReportByWeekUseCase = getWeeklyReportByWeekUseCase;
    }

    @Override
    public List<WeeklyReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }

        LocalDate weekStartDate = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<WeeklyReport> reports = new ArrayList<>();
        while (!weekStartDate.isAfter(to)) {
            try {
                reports.add(getWeeklyReportByWeekUseCase.execute(athleteId, coachId, weekStartDate));
            } catch (WeeklyReportNotFoundException e) {
                log.debug("No data for athleteId={} week={}, skipping", athleteId, weekStartDate);
            }
            weekStartDate = weekStartDate.plusWeeks(1);
        }
        return reports;
    }
}
