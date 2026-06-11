package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.exception.InvalidPeriodException;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.dto.response.LoadReportResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import com.axel.trainingmetricsapi.service.LoadReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Validated
@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/reports/load")
@RestController
public class LoadReportController {

    private final LoadReportWebMapper loadReportWebMapper;
    private final LoadReportService loadReportService;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;
    private final AthleteService athleteService;

    public LoadReportController(LoadReportWebMapper loadReportWebMapper,
                                LoadReportService loadReportService,
                                AuthenticatedCoachResolver authenticatedCoachResolver,
                                AthleteService athleteService) {
        this.loadReportWebMapper = loadReportWebMapper;
        this.loadReportService = loadReportService;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
        this.athleteService = athleteService;
    }

    @GetMapping(params = "weekStartDate")
    @Operation(summary = "Retrieve load report for a specific week")
    @ApiResponse(responseCode = "200", description = "Load report for the requested week (zero values if no sessions)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoadReportResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or future weekStartDate")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<LoadReportResponse> getByWeekStartDate(
            @PathVariable("id") long athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate weekStartDate) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        LoadReport report = loadReportService.findByAthleteIdAndWeekStartDate(athleteId, weekStartDate);
        return ResponseEntity.ok(loadReportWebMapper.domainToResponse(report));
    }

    @GetMapping("/latest")
    @Operation(summary = "Retrieve the most recent load report for an athlete")
    @ApiResponse(responseCode = "200", description = "Most recent load report found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoadReportResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "No load report found for this athlete")
    public ResponseEntity<LoadReportResponse> getLatest(@PathVariable("id") long athleteId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        LoadReport report = loadReportService.findLatestByAthleteId(athleteId);
        return ResponseEntity.ok(loadReportWebMapper.domainToResponse(report));
    }

    @GetMapping(params = "from")
    @Operation(summary = "Retrieve load reports for a period")
    @ApiResponse(responseCode = "200", description = "List of load reports in the period",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = LoadReportResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid period parameters")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<LoadReportResponse>> getByPeriod(
            @PathVariable("id") long athleteId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate to) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        LocalDate effectiveTo = Objects.requireNonNullElseGet(to, LocalDate::now);
        if (from.isAfter(effectiveTo)) {
            throw new InvalidPeriodException("from must be before or equal to to");
        }

        List<LoadReport> reports = loadReportService.findByAthleteIdAndPeriod(athleteId, from, effectiveTo);
        List<LoadReportResponse> responses = reports.stream()
            .map(loadReportWebMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }
}
