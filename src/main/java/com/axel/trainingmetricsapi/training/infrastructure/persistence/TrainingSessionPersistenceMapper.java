package com.axel.trainingmetricsapi.training.infrastructure.persistence;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrainingSessionPersistenceMapper {
    TrainingSessionJpaEntity domainToEntity(TrainingSession trainingSession);
    TrainingSession entityToDomain(TrainingSessionJpaEntity trainingSessionJpaEntity);
}
