package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.GetAcwrReportUseCase;
import com.axel.trainingmetricsapi.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.TrainingSession;
import com.axel.trainingmetricsapi.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetAcwrReportUseCaseImpl implements GetAcwrReportUseCase {

    private final AthleteRepository athleteRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final AcwrCachePort acwrCachePort;
    private final AcwrCalculator acwrCalculator;

    public GetAcwrReportUseCaseImpl(AthleteRepository athleteRepository,
                                    TrainingSessionRepository trainingSessionRepository,
                                    AcwrCachePort acwrCachePort) {
        this.athleteRepository = athleteRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.acwrCachePort = acwrCachePort;
        this.acwrCalculator = new AcwrCalculator();
    }

    @Override
    public AcwrReport execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return acwrCachePort.get(athleteId).orElseGet(() -> {
            LocalDate today = LocalDate.now();
            List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
                athleteId, today.minusDays(27), today);
            AcwrReport report = acwrCalculator.calculate(athleteId, sessions, today);
            acwrCachePort.put(athleteId, report);
            return report;
        });
    }
}
