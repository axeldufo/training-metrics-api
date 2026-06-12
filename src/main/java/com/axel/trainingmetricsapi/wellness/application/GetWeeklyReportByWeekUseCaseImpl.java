package com.axel.trainingmetricsapi.wellness.application;

import com.axel.trainingmetricsapi.wellness.application.port.in.GetWeeklyReportByWeekUseCase;
import com.axel.trainingmetricsapi.training.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportCalculator;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReport;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyReportCalculator;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.wellness.domain.exception.WeeklyReportNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetWeeklyReportByWeekUseCaseImpl implements GetWeeklyReportByWeekUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final LoadReportCalculator loadReportCalculator;
    private final WeeklyReportCalculator weeklyReportCalculator;

    public GetWeeklyReportByWeekUseCaseImpl(AthleteRepository athleteRepository,
                                             LoadReportRepository loadReportRepository,
                                             WeeklyWellnessRepository weeklyWellnessRepository,
                                             TrainingSessionRepository trainingSessionRepository) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.loadReportCalculator = new LoadReportCalculator();
        this.weeklyReportCalculator = new WeeklyReportCalculator(new AcwrCalculator());
    }

    @Override
    public WeeklyReport execute(long athleteId, long coachId, LocalDate weekStartDate) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }

        List<LoadReport> loadReports = loadReportRepository
            .findByAthleteIdAndWeekStartDateBetween(athleteId, weekStartDate.minusWeeks(3), weekStartDate)
            .stream()
            .sorted(Comparator.comparing(LoadReport::weekStartDate).reversed())
            .toList();

        List<WeeklyWellness> wellness = weeklyWellnessRepository
            .findByAthleteIdAndPeriod(athleteId, weekStartDate.minusWeeks(4), weekStartDate)
            .stream()
            .sorted(Comparator.comparing(WeeklyWellness::getWeekStartDate).reversed())
            .toList();

        if (loadReports.isEmpty()) {
            boolean wellnessAvailableForWeek = wellness.stream()
                .anyMatch(w -> w.getWeekStartDate().equals(weekStartDate));

            if (!wellnessAvailableForWeek) {
                throw new WeeklyReportNotFoundException(athleteId, weekStartDate);
            }

            List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
                athleteId, weekStartDate, weekStartDate.plusDays(6));
            LoadReport onTheFlyReport = loadReportCalculator.calculate(athleteId, weekStartDate, sessions, null);
            loadReports = List.of(onTheFlyReport);
        }

        return weeklyReportCalculator.calculate(athleteId, weekStartDate, loadReports, wellness);
    }
}
