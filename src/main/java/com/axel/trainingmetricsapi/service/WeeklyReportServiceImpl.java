package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.domain.WeeklyReportCalculator;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.WeeklyReportNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class WeeklyReportServiceImpl implements WeeklyReportService {

    private final LoadReportRepository loadReportRepository;
    private final WeeklyWellnessRepository weeklyWellnessRepository;
    private final LoadReportService loadReportService;
    private final WeeklyReportCalculator calculator;

    public WeeklyReportServiceImpl(
        LoadReportRepository loadReportRepository,
        WeeklyWellnessRepository weeklyWellnessRepository,
        LoadReportService loadReportService
    ) {
        this.loadReportRepository = loadReportRepository;
        this.weeklyWellnessRepository = weeklyWellnessRepository;
        this.loadReportService = loadReportService;
        this.calculator = new WeeklyReportCalculator(new AcwrCalculator());
    }

    @Override
    public WeeklyReport getWeeklyReport(long athleteId, LocalDate weekStartDate) {
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
                // No load data AND no wellness — nothing to report
                throw new WeeklyReportNotFoundException(athleteId, weekStartDate);
            }

            // Wellness exists — worth computing a zero-load report (athlete didn't train this week)
            LoadReport onTheFlyLoad = loadReportService.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
            loadReports = List.of(onTheFlyLoad);
        }

        return calculator.calculate(athleteId, weekStartDate, loadReports, wellness);
    }

    @Override
    public WeeklyReport getLatestWeeklyReport(long athleteId) {
        LoadReport latest = loadReportRepository.findLatestByAthleteId(athleteId)
            .orElseThrow(() -> new WeeklyReportNotFoundException(athleteId));
        return getWeeklyReport(athleteId, latest.weekStartDate());
    }

    @Override
    public List<WeeklyReport> getWeeklyReportsByPeriod(long athleteId, LocalDate from, LocalDate to) {
        LocalDate weekStartDate = from.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        List<WeeklyReport> reports = new ArrayList<>();
        while (!weekStartDate.isAfter(to)) {
            try {
                reports.add(getWeeklyReport(athleteId, weekStartDate));
            } catch (WeeklyReportNotFoundException e) {
                log.debug("No data for athleteId={} week={}, skipping", athleteId, weekStartDate);
            }
            weekStartDate = weekStartDate.plusWeeks(1);
        }
        return reports;
    }
}
