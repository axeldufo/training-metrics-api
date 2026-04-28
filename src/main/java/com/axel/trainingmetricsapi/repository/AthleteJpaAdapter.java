package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.domain.AthleteRepository;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AthleteJpaAdapter implements AthleteRepository {

    private final AthleteJpaRepository athleteJpaRepository;
    private final AthleteMapper athleteMapper;

    public AthleteJpaAdapter(AthleteJpaRepository athleteJpaRepository, AthleteMapper athleteMapper) {
        this.athleteJpaRepository = athleteJpaRepository;
        this.athleteMapper = athleteMapper;
    }

    @Override
    public Athlete save(Athlete athlete) {
        AthleteJpaEntity savedAthlete = athleteJpaRepository.save(athleteMapper.domainToEntity(athlete));
        return athleteMapper.entityToDomain(savedAthlete);
    }

    @Override
    public Optional<Athlete> findById(Long id) {
        return athleteJpaRepository.findById(id).map(athleteMapper::entityToDomain);
    }

    @Override
    public List<Athlete> findAll() {
        return athleteJpaRepository.findAll().stream().map(athleteMapper::entityToDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        athleteJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long athleteId) {
        return athleteJpaRepository.existsById(athleteId);
    }
}
