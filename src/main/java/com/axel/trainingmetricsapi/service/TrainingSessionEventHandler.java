package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TrainingSessionEventHandler {

    private final CacheManager cacheManager;
    private final AcwrReportService acwrReportService;
    private final TrainingSessionRepository trainingSessionRepository;
    private final LoadReportRepository loadReportRepository;

    public TrainingSessionEventHandler(CacheManager cacheManager,
                                       AcwrReportService acwrReportService,
                                       TrainingSessionRepository trainingSessionRepository,
                                       LoadReportRepository loadReportRepository) {
        this.cacheManager = cacheManager;
        this.acwrReportService = acwrReportService;
        this.trainingSessionRepository = trainingSessionRepository;
        this.loadReportRepository = loadReportRepository;
    }

    @EventListener
    public void onSessionCreated(TrainingSessionCreatedEvent event) {
        refreshAcwrReport(event.athleteId());
        recalculateLoadReport(event.athleteId(), event.date());
    }

    @EventListener
    public void onSessionUpdated(TrainingSessionUpdatedEvent event) {
        refreshAcwrReport(event.athleteId());
        recalculateLoadReport(event.athleteId(), event.date());
    }

    @EventListener
    public void onSessionDeleted(TrainingSessionDeletedEvent event) {
        refreshAcwrReport(event.athleteId());
        recalculateLoadReport(event.athleteId(), event.date());
    }

    private void refreshAcwrReport(long athleteId) {
        Cache cache = cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + CacheConfig.ACWR_REPORT_CACHE);
        }
        cache.evict(athleteId);
        acwrReportService.getAcwrReport(athleteId);
    }

    private void recalculateLoadReport(long athleteId, LocalDate sessionDate) {
        LocalDate weekStartDate = sessionDate.with(DayOfWeek.MONDAY);
        List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
            athleteId, weekStartDate, weekStartDate.plusDays(6));

        if (sessions.isEmpty()) {
            loadReportRepository.deleteByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
            return;
        }

        int totalFosterLoad = sessions.stream().mapToInt(TrainingSession::getFosterLoad).sum();
        loadReportRepository.save(new LoadReport(
            athleteId, weekStartDate, totalFosterLoad, sessions.size(), LocalDateTime.now()));
    }
}
