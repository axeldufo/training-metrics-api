package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionCreatedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionDeletedEvent;
import com.axel.trainingmetricsapi.domain.event.TrainingSessionUpdatedEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TrainingSessionEventHandler {

    private final CacheManager cacheManager;
    private final AcwrReportService acwrReportService;

    public TrainingSessionEventHandler(CacheManager cacheManager, AcwrReportService acwrReportService) {
        this.cacheManager = cacheManager;
        this.acwrReportService = acwrReportService;
    }

    @EventListener
    public void onSessionCreated(TrainingSessionCreatedEvent event) {
        refreshAcwrReport(event.athleteId());
    }

    @EventListener
    public void onSessionUpdated(TrainingSessionUpdatedEvent event) {
        refreshAcwrReport(event.athleteId());
    }

    @EventListener
    public void onSessionDeleted(TrainingSessionDeletedEvent event) {
        refreshAcwrReport(event.athleteId());
    }

    private void refreshAcwrReport(long athleteId) {
        Cache cache = cacheManager.getCache(CacheConfig.ACWR_REPORT_CACHE);
        if (cache == null) {
            throw new IllegalStateException("Cache not configured: " + CacheConfig.ACWR_REPORT_CACHE);
        }
        cache.evict(athleteId);

        acwrReportService.getAcwrReport(athleteId);
    }

}
