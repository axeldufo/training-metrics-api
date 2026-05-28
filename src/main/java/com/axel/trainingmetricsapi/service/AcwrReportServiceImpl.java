package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.config.CacheConfig;
import com.axel.trainingmetricsapi.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AcwrReportServiceImpl implements AcwrReportService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final AcwrCalculator acwrCalculator;

    public AcwrReportServiceImpl(TrainingSessionRepository trainingSessionRepository) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.acwrCalculator = new AcwrCalculator();
    }

    @Override
    @Cacheable(value = CacheConfig.ACWR_REPORT_CACHE, key = "#athleteId")
    public AcwrReport getAcwrReport(long athleteId) {
        LocalDate today = LocalDate.now();
        List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
            athleteId, today.minusDays(27), today);
        return acwrCalculator.calculate(athleteId, sessions, today);
    }
}
