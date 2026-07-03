package com.axel.trainingmetricsapi.wellness.interfaces.web;

import com.axel.trainingmetricsapi.wellness.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.wellness.interfaces.web.dto.WeeklyWellnessResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface WeeklyWellnessWebMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "athleteId", target = "athleteId")
    WeeklyWellness requestToDomain(WeeklyWellnessRequest request, long athleteId);

    WeeklyWellnessResponse domainToResponse(WeeklyWellness wellness);
}
