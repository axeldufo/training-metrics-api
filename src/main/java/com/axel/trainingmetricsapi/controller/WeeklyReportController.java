package com.axel.trainingmetricsapi.controller;

import com.axel.trainingmetricsapi.controller.exception.InvalidPeriodException;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.controller.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.dto.response.WeeklyReportResponse;
import com.axel.trainingmetricsapi.service.AthleteService;
import com.axel.trainingmetricsapi.service.WeeklyReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Validated
@RequestMapping(path = ApiConstants.API_VERSION + "/athletes/{id}/reports/weekly")
@RestController
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;
    private final WeeklyReportWebMapper weeklyReportWebMapper;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;
    private final AthleteService athleteService;

    public WeeklyReportController(WeeklyReportService weeklyReportService,
                                  WeeklyReportWebMapper weeklyReportWebMapper,
                                  AuthenticatedCoachResolver authenticatedCoachResolver,
                                  AthleteService athleteService) {
        this.weeklyReportService = weeklyReportService;
        this.weeklyReportWebMapper = weeklyReportWebMapper;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
        this.athleteService = athleteService;
    }

    @GetMapping(params = "weekStartDate")
    @Operation(summary = "Retrieve weekly report for a specific week")
    @ApiResponse(responseCode = "200", description = "Weekly report for the requested week",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeeklyReportResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid or future weekStartDate")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "No data found for this athlete on the requested week")
    public ResponseEntity<WeeklyReportResponse> getByWeekStartDate(
        @PathVariable("id") long athleteId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate weekStartDate) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        WeeklyReport report = weeklyReportService.getWeeklyReport(athleteId, weekStartDate);
        return ResponseEntity.ok(weeklyReportWebMapper.domainToResponse(report));
    }

    @GetMapping("/latest")
    @Operation(summary = "Retrieve the most recent weekly report for an athlete")
    @ApiResponse(responseCode = "200", description = "Most recent weekly report found",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = WeeklyReportResponse.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    @ApiResponse(responseCode = "404", description = "No weekly report found for this athlete")
    public ResponseEntity<WeeklyReportResponse> getLatest(@PathVariable("id") long athleteId) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        WeeklyReport report = weeklyReportService.getLatestWeeklyReport(athleteId);
        return ResponseEntity.ok(weeklyReportWebMapper.domainToResponse(report));
    }

    @GetMapping(params = "from")
    @Operation(summary = "Retrieve weekly reports for a period")
    @ApiResponse(responseCode = "200", description = "List of weekly reports in the period",
        content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = WeeklyReportResponse.class))))
    @ApiResponse(responseCode = "400", description = "Invalid period parameters")
    @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token")
    public ResponseEntity<List<WeeklyReportResponse>> getByPeriod(
        @PathVariable("id") long athleteId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PastOrPresent LocalDate to) {
        AuthenticatedCoach coach = authenticatedCoachResolver.resolve();
        athleteService.findById(athleteId, coach.id());

        LocalDate effectiveTo = Objects.requireNonNullElseGet(to, LocalDate::now);
        if (from.isAfter(effectiveTo)) {
            throw new InvalidPeriodException("from must be before or equal to to");
        }

        List<WeeklyReport> reports = weeklyReportService.getWeeklyReportsByPeriod(athleteId, from, effectiveTo);
        List<WeeklyReportResponse> responses = reports.stream()
            .map(weeklyReportWebMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }
}
