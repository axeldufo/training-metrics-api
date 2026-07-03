package com.axel.trainingmetricsapi.identity.interfaces.web;

import com.axel.trainingmetricsapi.identity.domain.Coach;
import com.axel.trainingmetricsapi.identity.interfaces.web.dto.CoachResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface CoachWebMapper {
    CoachResponse domainToResponse(Coach coach);
}
