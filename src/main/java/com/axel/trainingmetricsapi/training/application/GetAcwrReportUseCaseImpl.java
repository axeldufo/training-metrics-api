package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.in.GetAcwrReportUseCase;
import com.axel.trainingmetricsapi.training.application.port.out.AcwrCachePort;
import com.axel.trainingmetricsapi.training.domain.AcwrCalculator;
import com.axel.trainingmetricsapi.training.domain.AcwrReport;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
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
