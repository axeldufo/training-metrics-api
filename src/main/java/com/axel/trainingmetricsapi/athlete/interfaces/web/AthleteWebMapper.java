package com.axel.trainingmetricsapi.athlete.interfaces.web;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteRequest;
import com.axel.trainingmetricsapi.athlete.interfaces.web.dto.AthleteResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface AthleteWebMapper {

    AthleteResponse domainToResponse(Athlete athlete);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "coachId", target = "coachId")
    Athlete requestToDomain(AthleteRequest request, long coachId);
}
