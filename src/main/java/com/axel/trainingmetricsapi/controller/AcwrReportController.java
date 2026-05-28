package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.AcwrReport;
import com.axel.trainingmetricsapi.dto.response.AcwrReportResponse;
import com.axel.trainingmetricsapi.service.AcwrReportService;
import com.axel.trainingmetricsapi.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/reports")
@RestController
public class AcwrReportController {

    private final AcwrReportWebMapper acwrReportWebMapper;
    private final AcwrReportService acwrReportService;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;
    private final AthleteService athleteService;

    public AcwrReportController(AcwrReportWebMapper acwrReportWebMapper,
                                AcwrReportService acwrReportService,
                                AuthenticatedCoachResolver authenticatedCoachResolver,
                                AthleteService athleteService) {
        this.acwrReportWebMapper = acwrReportWebMapper;
        this.acwrReportService = acwrReportService;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
        this.athleteService = athleteService;
    }

    @GetMapping("/acwr")
    @Operation(summary = "Retrieve ACWR report for an athlete")
    @ApiResponse(responseCode = "200", description = "ACWR report calculated and returned",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = AcwrReportResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "Athlete not found")
    public ResponseEntity<AcwrReportResponse> getAcwrReport(@PathVariable("id") long athleteId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        AcwrReport report = acwrReportService.getAcwrReport(athleteId);
        return ResponseEntity.ok(acwrReportWebMapper.domainToResponse(report));
    }
}
