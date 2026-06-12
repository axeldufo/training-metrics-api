package com.axel.trainingmetricsapi.application.training;

import com.axel.trainingmetricsapi.application.port.in.GetLoadReportsByPeriodUseCase;
import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetLoadReportsByPeriodUseCaseImpl implements GetLoadReportsByPeriodUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;

    public GetLoadReportsByPeriodUseCaseImpl(AthleteRepository athleteRepository,
                                              LoadReportRepository loadReportRepository) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
    }

    @Override
    public List<LoadReport> execute(long athleteId, long coachId, LocalDate from, LocalDate to) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return loadReportRepository.findByAthleteIdAndWeekStartDateBetween(athleteId, from, to);
    }
}
