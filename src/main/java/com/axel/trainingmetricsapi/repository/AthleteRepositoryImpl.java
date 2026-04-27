package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Athlete;
import com.axel.trainingmetricsapi.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.mapper.AthleteMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AthleteRepositoryImpl implements AthleteRepository {

    private final AthleteJpaRepository athleteJpaRepository;
    private final AthleteMapper athleteMapper;

    public AthleteRepositoryImpl(AthleteJpaRepository athleteJpaRepository, AthleteMapper athleteMapper) {
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
        if (!athleteJpaRepository.existsById(id)) {
            throw new AthleteNotFoundException(id);
        }
        athleteJpaRepository.deleteById(id);
    }
}
