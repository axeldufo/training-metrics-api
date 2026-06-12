package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.domain.AcwrAlert;
import com.axel.trainingmetricsapi.domain.CorrelationAlert;
import com.axel.trainingmetricsapi.domain.WeeklyReport;
import com.axel.trainingmetricsapi.domain.WellnessAlert;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.WeeklyReportResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class WeeklyReportWebMapperTest {

    private static final long ATHLETE_ID = 1L;
    private static final LocalDate MONDAY = LocalDate.of(2025, Month.MAY, 19);

    private final WeeklyReportWebMapper mapper = new WeeklyReportWebMapper();

    @Test
    void domainToResponse_nominal_allFieldsMapped() {
        WeeklyReport domain = new WeeklyReport(
            ATHLETE_ID, MONDAY, true,
            200, 3,
            200.0, 160.0, 1.25,
            AcwrAlert.OK, true,
            4, 3, 5,
            1.0, -1.0, 0.0,
            Set.of(WellnessAlert.ABSOLUTE_LOW),
            Set.of(),
            Set.of(WellnessAlert.TREND_DECLINING),
            CorrelationAlert.GOOD_ADAPTATION
        );

        WeeklyReportResponse response = mapper.domainToResponse(domain);

        assertThat(response.athleteId()).isEqualTo(ATHLETE_ID);
        assertThat(response.weekStartDate()).isEqualTo(MONDAY);
        assertThat(response.wellnessAvailable()).isTrue();
        assertThat(response.totalFosterLoad()).isEqualTo(200);
        assertThat(response.sessionCount()).isEqualTo(3);
        assertThat(response.acuteLoad()).isEqualTo(200.0);
        assertThat(response.chronicLoad()).isEqualTo(160.0);
        assertThat(response.acwr()).isEqualTo(1.25);
        assertThat(response.acwrAlert()).isEqualTo(AcwrAlert.OK);
        assertThat(response.acwrReliable()).isTrue();
        assertThat(response.perceivedDifficulty()).isEqualTo(4);
        assertThat(response.perceivedFatigue()).isEqualTo(3);
        assertThat(response.motivation()).isEqualTo(5);
        assertThat(response.deltaDifficulty()).isEqualTo(1.0);
        assertThat(response.deltaFatigue()).isEqualTo(-1.0);
        assertThat(response.deltaMotivation()).isEqualTo(0.0);
        assertThat(response.difficultyAlerts()).containsExactly(WellnessAlert.ABSOLUTE_LOW);
        assertThat(response.fatigueAlerts()).isEmpty();
        assertThat(response.motivationAlerts()).containsExactly(WellnessAlert.TREND_DECLINING);
        assertThat(response.correlationAlert()).isEqualTo(CorrelationAlert.GOOD_ADAPTATION);
    }

    @Test
    void domainToResponse_nullableFields_mappedAsNull() {
        WeeklyReport domain = new WeeklyReport(
            ATHLETE_ID, MONDAY, false,
            0, 0,
            0.0, 0.0, 0.0,
            AcwrAlert.NO_DATA, false,
            null, null, null,
            null, null, null,
            Set.of(), Set.of(), Set.of(),
            CorrelationAlert.INSUFFICIENT_DATA
        );

        WeeklyReportResponse response = mapper.domainToResponse(domain);

        assertThat(response.perceivedDifficulty()).isNull();
        assertThat(response.perceivedFatigue()).isNull();
        assertThat(response.motivation()).isNull();
        assertThat(response.deltaDifficulty()).isNull();
        assertThat(response.deltaFatigue()).isNull();
        assertThat(response.deltaMotivation()).isNull();
        assertThat(response.correlationAlert()).isEqualTo(CorrelationAlert.INSUFFICIENT_DATA);
    }

    @Test
    void domainToResponse_emptyAlertSets_mappedAsEmptySets_notNull() {
        WeeklyReport domain = new WeeklyReport(
            ATHLETE_ID, MONDAY, true,
            100, 1,
            100.0, 100.0, 1.0,
            AcwrAlert.OK, true,
            3, 3, 4,
            0.0, 0.0, 0.0,
            Set.of(), Set.of(), Set.of(),
            CorrelationAlert.NO_ALERT
        );

        WeeklyReportResponse response = mapper.domainToResponse(domain);

        assertThat(response.difficultyAlerts()).isNotNull().isEmpty();
        assertThat(response.fatigueAlerts()).isNotNull().isEmpty();
        assertThat(response.motivationAlerts()).isNotNull().isEmpty();
    }
}
