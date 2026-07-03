package com.axel.trainingmetricsapi.wellness.infrastructure.persistence;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WeeklyWellnessPersistenceMapper {
    WeeklyWellnessJpaEntity domainToEntity(WeeklyWellness wellness);
    WeeklyWellness entityToDomain(WeeklyWellnessJpaEntity entity);
}
