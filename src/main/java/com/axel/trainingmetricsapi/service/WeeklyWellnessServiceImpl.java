package com.axel.trainingmetricsapi.service;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.WeeklyWellnessRepository;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class WeeklyWellnessServiceImpl implements WeeklyWellnessService {

    private final WeeklyWellnessRepository wellnessRepository;

    public WeeklyWellnessServiceImpl(WeeklyWellnessRepository wellnessRepository) {
        this.wellnessRepository = wellnessRepository;
    }

    @Override
    @Transactional
    public WeeklyWellness save(WeeklyWellness wellness) {
        if (wellnessRepository.existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate())) {
            throw new WeeklyWellnessAlreadyExistsException(wellness.getAthleteId(), wellness.getWeekStartDate());
        }
        return wellnessRepository.save(wellness);
    }

    @Override
    public List<WeeklyWellness> findByAthleteIdAndPeriod(long athleteId, LocalDate from, LocalDate to) {
        return wellnessRepository.findByAthleteIdAndPeriod(athleteId, from, to);
    }

    @Override
    public WeeklyWellness findById(long id, long athleteId) {
        WeeklyWellness wellness = wellnessRepository.findById(id)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(id));
        if (wellness.getAthleteId() != athleteId) {
            // Return 404 instead of 403 to prevent id enumeration
            throw new WeeklyWellnessNotFoundException(id);
        }
        return wellness;
    }

    @Override
    @Transactional
    public WeeklyWellness update(WeeklyWellness wellness) {
        long id = wellness.getId();
        WeeklyWellness existing = wellnessRepository.findById(id)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(id));
        if (existing.getAthleteId() != wellness.getAthleteId()) {
            // Return 404 instead of 403 to prevent id enumeration
            throw new WeeklyWellnessNotFoundException(id);
        }
        if (!wellness.getWeekStartDate().equals(existing.getWeekStartDate())
            && wellnessRepository.existsByAthleteIdAndWeekStartDate(wellness.getAthleteId(), wellness.getWeekStartDate())) {
            throw new WeeklyWellnessAlreadyExistsException(wellness.getAthleteId(), wellness.getWeekStartDate());
        }
        return wellnessRepository.save(wellness);
    }

    @Override
    @Transactional
    public void deleteById(long id, long athleteId) {
        WeeklyWellness existing = wellnessRepository.findById(id)
            .orElseThrow(() -> new WeeklyWellnessNotFoundException(id));
        if (existing.getAthleteId() != athleteId) {
            // Return 404 instead of 403 to prevent id enumeration
            throw new WeeklyWellnessNotFoundException(id);
        }
        wellnessRepository.deleteById(id);
    }
}
