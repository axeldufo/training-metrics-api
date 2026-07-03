package com.axel.trainingmetricsapi.athlete.infrastructure.persistence;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AthletePersistenceMapper {
    AthleteJpaEntity domainToEntity(Athlete athlete);
    Athlete entityToDomain(AthleteJpaEntity entity);
}
