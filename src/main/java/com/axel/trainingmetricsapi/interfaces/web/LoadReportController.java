package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.application.port.in.GetLatestLoadReportUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetLoadReportByWeekUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetLoadReportsByPeriodUseCase;
import com.axel.trainingmetricsapi.interfaces.web.exception.InvalidPeriodException;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.LoadReport;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.LoadReportResponse;
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
    private final GetLoadReportByWeekUseCase getLoadReportByWeekUseCase;
    private final GetLatestLoadReportUseCase getLatestLoadReportUseCase;
    private final GetLoadReportsByPeriodUseCase getLoadReportsByPeriodUseCase;
    private final AuthenticatedCoachResolver authenticatedCoachResolver;

    public LoadReportController(LoadReportWebMapper loadReportWebMapper,
                                GetLoadReportByWeekUseCase getLoadReportByWeekUseCase,
                                GetLatestLoadReportUseCase getLatestLoadReportUseCase,
                                GetLoadReportsByPeriodUseCase getLoadReportsByPeriodUseCase,
                                AuthenticatedCoachResolver authenticatedCoachResolver) {
        this.loadReportWebMapper = loadReportWebMapper;
        this.getLoadReportByWeekUseCase = getLoadReportByWeekUseCase;
        this.getLatestLoadReportUseCase = getLatestLoadReportUseCase;
        this.getLoadReportsByPeriodUseCase = getLoadReportsByPeriodUseCase;
        this.authenticatedCoachResolver = authenticatedCoachResolver;
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
        long coachId = coach.id();
        LoadReport report = getLoadReportByWeekUseCase.execute(athleteId, coachId, weekStartDate);
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
        long coachId = coach.id();
        LoadReport report = getLatestLoadReportUseCase.execute(athleteId, coachId);
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
        long coachId = coach.id();
        LocalDate effectiveTo = Objects.requireNonNullElseGet(to, LocalDate::now);
        if (from.isAfter(effectiveTo)) {
            throw new InvalidPeriodException("from must be before or equal to to");
        }
        List<LoadReport> reports = getLoadReportsByPeriodUseCase.execute(athleteId, coachId, from, effectiveTo);
        List<LoadReportResponse> responses = reports.stream()
            .map(loadReportWebMapper::domainToResponse)
            .toList();
        return ResponseEntity.ok(responses);
    }
}
