package com.axel.trainingmetricsapi.infrastructure.persistence;

import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.domain.LoadReportRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class LoadReportJpaAdapter implements LoadReportRepository {

    private final LoadReportJpaRepository loadReportJpaRepository;
    private final LoadReportPersistenceMapper persistenceMapper;

    public LoadReportJpaAdapter(LoadReportJpaRepository loadReportJpaRepository,
                                LoadReportPersistenceMapper persistenceMapper) {
        this.loadReportJpaRepository = loadReportJpaRepository;
        this.persistenceMapper = persistenceMapper;
    }

    @Override
    @Transactional
    public LoadReport save(LoadReport report) {
        LoadReportJpaEntity entityToSave = persistenceMapper.domainToEntity(report);
        // Upsert: set existing entity id so JPA issues an UPDATE instead of INSERT
        loadReportJpaRepository.findByAthleteIdAndWeekStartDate(report.athleteId(), report.weekStartDate())
            .ifPresent(existing -> entityToSave.setId(existing.getId()));
        LoadReportJpaEntity saved = loadReportJpaRepository.save(entityToSave);
        return persistenceMapper.entityToDomain(saved);
    }

    @Override
    public Optional<LoadReport> findByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate) {
        return loadReportJpaRepository.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate)
            .map(persistenceMapper::entityToDomain);
    }

    @Override
    public Optional<LoadReport> findLatestByAthleteId(long athleteId) {
        return loadReportJpaRepository.findFirstByAthleteIdOrderByWeekStartDateDesc(athleteId)
            .map(persistenceMapper::entityToDomain);
    }

    @Override
    public List<LoadReport> findByAthleteIdAndWeekStartDateBetween(long athleteId, LocalDate from, LocalDate to) {
        return loadReportJpaRepository.findAllByAthleteIdAndWeekStartDateBetween(athleteId, from, to)
            .stream()
            .map(persistenceMapper::entityToDomain)
            .toList();
    }

    @Override
    @Transactional
    public void deleteByAthleteIdAndWeekStartDate(long athleteId, LocalDate weekStartDate) {
        loadReportJpaRepository.deleteByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
    }
}
