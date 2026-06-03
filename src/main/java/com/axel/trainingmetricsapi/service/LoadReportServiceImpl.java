package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.*;
import com.axel.trainingmetricsapi.domain.exception.LoadReportNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class LoadReportServiceImpl implements LoadReportService {

    private final LoadReportRepository loadReportRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final LoadReportCalculator loadReportCalculator;

    public LoadReportServiceImpl(LoadReportRepository loadReportRepository,
                                 TrainingSessionRepository trainingSessionRepository) {
        this.loadReportRepository = loadReportRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.loadReportCalculator = new LoadReportCalculator();
    }

    @Override
    public LoadReport findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate) {
        return loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
            .orElseGet(() -> calculateOnTheFly(athleteId, weekStartDate));
    }

    @Override
    public LoadReport findLatestByAthleteId(long athleteId) {
        return loadReportRepository.findLatestByAthleteId(athleteId)
            .orElseThrow(() -> new LoadReportNotFoundException(athleteId));
    }

    @Override
    public List<LoadReport> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to) {
        return loadReportRepository.findByAthleteIdAndWeekStartDateBetween(athleteId, from, to);
    }

    private LoadReport calculateOnTheFly(long athleteId, LocalDate weekStartDate) {
        List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
            athleteId, weekStartDate, weekStartDate.plusDays(6));

        if (!sessions.isEmpty()) {
            log.warn("LoadReport not found in DB for athleteId={} weekStartDate={} but {} session(s) exist " +
                "— event chain may be broken", athleteId, weekStartDate, sessions.size());
            return loadReportCalculator.calculate(athleteId, weekStartDate, sessions, null);
        }

        return new LoadReport(athleteId, weekStartDate, 0, 0, null);
    }
}
