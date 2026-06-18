package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.application.port.in.GetLoadReportByWeekUseCase;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportCalculator;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.domain.TrainingSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetLoadReportByWeekUseCaseImpl implements GetLoadReportByWeekUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final LoadReportCalculator loadReportCalculator;

    public GetLoadReportByWeekUseCaseImpl(AthleteRepository athleteRepository,
                                          LoadReportRepository loadReportRepository,
                                          TrainingSessionRepository trainingSessionRepository) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.loadReportCalculator = new LoadReportCalculator();
    }

    @Override
    public LoadReport execute(long athleteId, long coachId, LocalDate weekStartDate) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return loadReportRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
            .orElseGet(() -> {
                List<TrainingSession> sessions = trainingSessionRepository.findByAthleteIdAndPeriod(
                    athleteId, weekStartDate, weekStartDate.plusDays(6));
                return loadReportCalculator.calculate(athleteId, weekStartDate, sessions, null);
            });
    }
}
