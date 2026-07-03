package com.axel.trainingmetricsapi.training.interfaces.web;

import com.axel.trainingmetricsapi.training.domain.TrainingSession;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionRequest;
import com.axel.trainingmetricsapi.training.interfaces.web.dto.TrainingSessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface TrainingSessionWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "athleteId", target = "athleteId")
    TrainingSession requestToDomain(TrainingSessionRequest trainingSessionRequest, long athleteId);

    @Mapping(source = "aboveTargetZone", target = "aboveTargetAlert")
    @Mapping(source = "belowTargetZone", target = "belowTargetAlert")
    TrainingSessionResponse domainToResponse(TrainingSession trainingSession);
}
