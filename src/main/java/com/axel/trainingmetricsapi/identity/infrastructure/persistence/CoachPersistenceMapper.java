package com.axel.trainingmetricsapi.identity.infrastructure.persistence;

import com.axel.trainingmetricsapi.identity.domain.Coach;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface  CoachPersistenceMapper {
    Coach entityToDomain(CoachJpaEntity coachJpaEntity);
}
