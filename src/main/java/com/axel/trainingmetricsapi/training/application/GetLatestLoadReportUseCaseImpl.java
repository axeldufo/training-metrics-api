package com.axel.trainingmetricsapi.training.application;

import com.axel.trainingmetricsapi.training.application.port.in.GetLatestLoadReportUseCase;
import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.training.domain.LoadReport;
import com.axel.trainingmetricsapi.training.domain.LoadReportRepository;
import com.axel.trainingmetricsapi.athlete.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.training.domain.exception.LoadReportNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetLatestLoadReportUseCaseImpl implements GetLatestLoadReportUseCase {

    private final AthleteRepository athleteRepository;
    private final LoadReportRepository loadReportRepository;

    public GetLatestLoadReportUseCaseImpl(AthleteRepository athleteRepository,
                                          LoadReportRepository loadReportRepository) {
        this.athleteRepository = athleteRepository;
        this.loadReportRepository = loadReportRepository;
    }

    @Override
    public LoadReport execute(long athleteId, long coachId) {
        Athlete athlete = athleteRepository.findById(athleteId)
            .orElseThrow(() -> new AthleteNotFoundException(athleteId));
        if (athlete.getCoachId() != coachId) {
            throw new AthleteNotFoundException(athleteId);
        }
        return loadReportRepository.findLatestByAthleteId(athleteId)
            .orElseThrow(() -> new LoadReportNotFoundException(athleteId));
    }
}
