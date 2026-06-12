package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.in.TrainingSessionChangedUseCase;
import com.axel.trainingmetricsapi.training.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.training.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportCalculator;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingSessionChangedUseCaseImpl implements TrainingSessionChangedUseCase {

    private final TrainingSessionRepository trainingSessionRepository;
    private final AcwrCachePort acwrCachePort;
    private final AcwrCalculator acwrCalculator;
    private final LoadReportRepository loadReportRepository;
    private final LoadReportCalculator loadReportCalculator;

    public TrainingSessionChangedUseCaseImpl(TrainingSessionRepository trainingSessionRepository, AcwrCachePort acwrCachePort, LoadReportRepository loadReportRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.acwrCachePort = acwrCachePort;
        this.acwrCalculator = new AcwrCalculator();
        this.loadReportRepository = loadReportRepository;
        this.loadReportCalculator = new LoadReportCalculator();
    }

    @Override
    public void execute(long athleteId, LocalDate date) {
        refreshAcwrReport(athleteId);
        recalculateLoadReport(athleteId, date);
    }

    private void refreshAcwrReport(long athleteId) {
        acwrCachePort.evict(athleteId);

        LocalDate today = LocalDate.now();
        List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(athleteId, today.minusDays(27), today);
        AcwrReport report = acwrCalculator.calculate(athleteId, sessions, today);
        acwrCachePort.put(athleteId, report);
    }

    private void recalculateLoadReport(long athleteId, LocalDate sessionDate) {
        LocalDate weekStartDate = sessionDate.with(DayOfWeek.MONDAY);
        List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
            athleteId, weekStartDate, weekStartDate.plusDays(6));

        if (sessions.isEmpty()) {
            loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
            return;
        }

        loadReportRepository.save(
            loadReportCalculator.calculate(athleteId, weekStartDate, sessions, LocalDateTime.now()));
    }
}
