package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AcwrReportServiceImpl implements AcwrReportService {

    private final TrainingSessionRepository trainingSessionRepository;
    private final AcwrCalculator acwrCalculator;
    private final AcwrCachePort acwrCachePort;

    public AcwrReportServiceImpl(TrainingSessionRepository trainingSessionRepository, AcwrCachePort acwrCachePort) {
        this.trainingSessionRepository = trainingSessionRepository;
        this.acwrCachePort = acwrCachePort;
        this.acwrCalculator = new AcwrCalculator();
    }

    @Override
    public AcwrReport getAcwrReport(long athleteId) {
        return acwrCachePort.get(athleteId)
            .orElseGet(() -> {
                LocalDate today = LocalDate.now();
                List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
                    athleteId, today.minusDays(27), today);
                AcwrReport report = acwrCalculator.calculate(athleteId, sessions, today);
                acwrCachePort.put(athleteId, report);
                return report;
            });
    }
}
