package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.in.GetLoadReportsByPeriodUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import io.micrometer.observation.annotation.Observed;
import java.util.List;

@Service
public class GetLoadReportsByPeriodUseCaseImpl implements GetLoadReportsByPeriodUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;

    public GetLoadReportsByPeriodUseCaseImpl(AthleteRepository athleteRepository,
                                              LoadReportRepository loadReportRepository) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
    }

    @Override
    @Transactional(readOnly = true)
    @Observed(name = "use-case.get-load-reports-by-period")
    public List<LoadReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return loadReportRepository.findByAthleteIdAndWeekStartDateBetween(athleteId, from, to);
    }
}
