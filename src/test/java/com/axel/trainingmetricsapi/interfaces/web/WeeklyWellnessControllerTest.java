package com.axel.trainingmetricsapi.interfaces.web;

import com.axel.trainingmetricsapi.application.port.in.CreateWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.application.port.in.DeleteWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.application.port.in.GetWeeklyWellnessesByPeriodUseCase;
import com.axel.trainingmetricsapi.application.port.in.UpdateWeeklyWellnessUseCase;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoach;
import com.axel.trainingmetricsapi.interfaces.web.security.AuthenticatedCoachResolver;
import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import com.axel.trainingmetricsapi.domain.exception.AthleteNotFoundException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessAlreadyExistsException;
import com.axel.trainingmetricsapi.domain.exception.WeeklyWellnessNotFoundException;
import com.axel.trainingmetricsapi.interfaces.web.dto.request.WeeklyWellnessRequest;
import com.axel.trainingmetricsapi.interfaces.web.dto.response.WeeklyWellnessResponse;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeeklyWellnessController.class)
class WeeklyWellnessControllerTest extends SecurityMockControllerSupport {

    private static final long ATHLETE_ID = 4L;
    private static final long COACH_ID = 2L;
    private static final String URL_PREFIX = ApiConstants.API_VERSION + "/athletes/" + ATHLETE_ID + "/wellness";

    @MockitoBean
    private WeeklyWellnessWebMapper wellnessWebMapper;

    @MockitoBean
    private CreateWeeklyWellnessUseCase createWeeklyWellnessUseCase;

    @MockitoBean
    private GetWeeklyWellnessUseCase getWeeklyWellnessUseCase;

    @MockitoBean
    private GetWeeklyWellnessesByPeriodUseCase getWeeklyWellnessesByPeriodUseCase;

    @MockitoBean
    private UpdateWeeklyWellnessUseCase updateWeeklyWellnessUseCase;

    @MockitoBean
    private DeleteWeeklyWellnessUseCase deleteWeeklyWellnessUseCase;

    @MockitoBean
    private AuthenticatedCoachResolver authenticatedCoachResolver;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        WeeklyWellness persisted = aValidWellness();
        when(createWeeklyWellnessUseCase.execute(wellness, COACH_ID)).thenReturn(persisted);
        WeeklyWellnessResponse response = Instancio.of(WeeklyWellnessResponse.class)
            .set(field(WeeklyWellnessResponse::athleteId), ATHLETE_ID)
            .create();
        when(wellnessWebMapper.domainToResponse(persisted)).thenReturn(response);

        ResultActions result = mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", URL_PREFIX + "/" + response.id()));

        assertJsonMatchesResponse(result, response);
        verify(wellnessWebMapper).requestToDomain(request, ATHLETE_ID);
        verify(createWeeklyWellnessUseCase).execute(wellness, COACH_ID);
        verify(wellnessWebMapper).domainToResponse(persisted);
    }

    @Test
    void create_shouldReturn400_whenRequestIsInvalid() throws Exception {
        WeeklyWellnessRequest invalidRequest = new WeeklyWellnessRequest(LocalDate.now().plusWeeks(1), 0, 6, null);

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void create_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        when(createWeeklyWellnessUseCase.execute(wellness, COACH_ID))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(createWeeklyWellnessUseCase).execute(wellness, COACH_ID);
    }

    @Test
    void create_shouldReturn409_whenWellnessAlreadyExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        when(createWeeklyWellnessUseCase.execute(wellness, COACH_ID))
            .thenThrow(new WeeklyWellnessAlreadyExistsException(ATHLETE_ID, wellness.getWeekStartDate()));

        mvc.perform(post(URL_PREFIX).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$[0].code").value("WELLNESS_ALREADY_EXISTS"));

        verify(createWeeklyWellnessUseCase).execute(wellness, COACH_ID);
    }

    @Test
    void getByPeriod_shouldReturn200WithList_whenRequestIsValid() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 29);
        int count = 2;
        List<WeeklyWellness> wellnessList = Instancio.ofList(WeeklyWellness.class).size(count).create();
        when(getWeeklyWellnessesByPeriodUseCase.execute(ATHLETE_ID, COACH_ID, from, to)).thenReturn(wellnessList);
        when(wellnessWebMapper.domainToResponse(any(WeeklyWellness.class)))
            .thenReturn(Instancio.create(WeeklyWellnessResponse.class));

        mvc.perform(get(URL_PREFIX)
            .param("from", from.toString())
            .param("to", to.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(count)));

        verify(getWeeklyWellnessesByPeriodUseCase).execute(ATHLETE_ID, COACH_ID, from, to);
        verify(wellnessWebMapper, times(count)).domainToResponse(any(WeeklyWellness.class));
    }

    @Test
    void getByPeriod_shouldUseToday_whenToIsAbsent() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 1);
        when(getWeeklyWellnessesByPeriodUseCase.execute(eq(ATHLETE_ID), eq(COACH_ID), eq(from), any(LocalDate.class)))
            .thenReturn(List.of());

        mvc.perform(get(URL_PREFIX).param("from", from.toString()))
            .andExpect(status().isOk());

        ArgumentCaptor<LocalDate> toCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(getWeeklyWellnessesByPeriodUseCase).execute(eq(ATHLETE_ID), eq(COACH_ID), eq(from), toCaptor.capture());
        assertThat(toCaptor.getValue()).isEqualTo(LocalDate.now());
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsAfterTo() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        LocalDate from = LocalDate.of(2024, Month.JANUARY, 20);
        LocalDate to = LocalDate.of(2024, Month.JANUARY, 13);

        mvc.perform(get(URL_PREFIX)
            .param("from", from.toString())
            .param("to", to.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"))
            .andExpect(jsonPath("$[0].field").value("from"));

        verify(getWeeklyWellnessesByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldReturn400_whenFromIsMissing() throws Exception {
        mvc.perform(get(URL_PREFIX))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].code").value("HTTP_VALIDATION_ERROR"));

        verify(getWeeklyWellnessesByPeriodUseCase, never()).execute(anyLong(), anyLong(), any(), any());
    }

    @Test
    void getByPeriod_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getWeeklyWellnessesByPeriodUseCase.execute(eq(ATHLETE_ID), eq(COACH_ID), any(), any()))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX)
            .param("from", "2024-01-01"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getWeeklyWellnessesByPeriodUseCase).execute(eq(ATHLETE_ID), eq(COACH_ID), any(), any());
    }

    @Test
    void getById_shouldReturn200_whenWellnessExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        WeeklyWellness wellness = aValidWellness();
        when(getWeeklyWellnessUseCase.execute(wellnessId, ATHLETE_ID, COACH_ID)).thenReturn(wellness);
        WeeklyWellnessResponse response = Instancio.create(WeeklyWellnessResponse.class);
        when(wellnessWebMapper.domainToResponse(wellness)).thenReturn(response);

        ResultActions result = mvc.perform(get(URL_PREFIX + "/" + wellnessId))
            .andExpect(status().isOk());

        assertJsonMatchesResponse(result, response);
        verify(getWeeklyWellnessUseCase).execute(wellnessId, ATHLETE_ID, COACH_ID);
        verify(wellnessWebMapper).domainToResponse(wellness);
    }

    @Test
    void getById_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        when(getWeeklyWellnessUseCase.execute(anyLong(), eq(ATHLETE_ID), eq(COACH_ID)))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(get(URL_PREFIX + "/8"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getWeeklyWellnessUseCase).execute(anyLong(), eq(ATHLETE_ID), eq(COACH_ID));
    }

    @Test
    void getById_shouldReturn404_whenWellnessNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        when(getWeeklyWellnessUseCase.execute(wellnessId, ATHLETE_ID, COACH_ID))
            .thenThrow(new WeeklyWellnessNotFoundException(wellnessId));

        mvc.perform(get(URL_PREFIX + "/" + wellnessId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(getWeeklyWellnessUseCase).execute(wellnessId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void updateById_shouldReturn200_whenRequestIsValid() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        WeeklyWellness updated = aValidWellness();
        when(updateWeeklyWellnessUseCase.execute(wellness, COACH_ID)).thenReturn(updated);
        WeeklyWellnessResponse response = Instancio.create(WeeklyWellnessResponse.class);
        when(wellnessWebMapper.domainToResponse(updated)).thenReturn(response);

        ResultActions result = mvc.perform(put(URL_PREFIX + "/" + wellnessId)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());

        assertJsonMatchesResponse(result, response);
        verify(wellnessWebMapper).requestToDomain(request, ATHLETE_ID);
        verify(updateWeeklyWellnessUseCase).execute(wellness, COACH_ID);
        verify(wellnessWebMapper).domainToResponse(updated);
    }

    @Test
    void updateById_shouldReturn400_whenRequestIsInvalid() throws Exception {
        WeeklyWellnessRequest invalidRequest = new WeeklyWellnessRequest(null, 0, 6, null);

        mvc.perform(put(URL_PREFIX + "/8").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    void updateById_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        when(updateWeeklyWellnessUseCase.execute(wellness, COACH_ID))
            .thenThrow(new AthleteNotFoundException(ATHLETE_ID));

        mvc.perform(put(URL_PREFIX + "/8").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(updateWeeklyWellnessUseCase).execute(wellness, COACH_ID);
    }

    @Test
    void updateById_shouldReturn404_whenWellnessNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        when(updateWeeklyWellnessUseCase.execute(wellness, COACH_ID))
            .thenThrow(new WeeklyWellnessNotFoundException(wellnessId));

        mvc.perform(put(URL_PREFIX + "/" + wellnessId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(updateWeeklyWellnessUseCase).execute(wellness, COACH_ID);
    }

    @Test
    void updateById_shouldReturn409_whenWellnessAlreadyExists() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        WeeklyWellnessRequest request = aValidRequest();
        WeeklyWellness wellness = aValidWellness();
        when(wellnessWebMapper.requestToDomain(request, ATHLETE_ID)).thenReturn(wellness);
        when(updateWeeklyWellnessUseCase.execute(wellness, COACH_ID))
            .thenThrow(new WeeklyWellnessAlreadyExistsException(ATHLETE_ID, wellness.getWeekStartDate()));

        mvc.perform(put(URL_PREFIX + "/" + wellnessId).contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$[0].code").value("WELLNESS_ALREADY_EXISTS"));

        verify(updateWeeklyWellnessUseCase).execute(wellness, COACH_ID);
    }

    @Test
    void deleteById_shouldReturn204_whenWellnessDeleted() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;

        mvc.perform(delete(URL_PREFIX + "/" + wellnessId))
            .andExpect(status().isNoContent());

        verify(deleteWeeklyWellnessUseCase).execute(wellnessId, ATHLETE_ID, COACH_ID);
    }

    @Test
    void deleteById_shouldReturn404_whenAthleteNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        doThrow(new AthleteNotFoundException(ATHLETE_ID))
            .when(deleteWeeklyWellnessUseCase).execute(anyLong(), eq(ATHLETE_ID), eq(COACH_ID));

        mvc.perform(delete(URL_PREFIX + "/8"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteWeeklyWellnessUseCase).execute(anyLong(), eq(ATHLETE_ID), eq(COACH_ID));
    }

    @Test
    void deleteById_shouldReturn404_whenWellnessNotFound() throws Exception {
        when(authenticatedCoachResolver.resolve()).thenReturn(new AuthenticatedCoach(COACH_ID));
        long wellnessId = 8L;
        doThrow(new WeeklyWellnessNotFoundException(wellnessId))
            .when(deleteWeeklyWellnessUseCase).execute(wellnessId, ATHLETE_ID, COACH_ID);

        mvc.perform(delete(URL_PREFIX + "/" + wellnessId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$[0].code").value("NOT_FOUND"));

        verify(deleteWeeklyWellnessUseCase).execute(wellnessId, ATHLETE_ID, COACH_ID);
    }

    private WeeklyWellnessRequest aValidRequest() {
        return Instancio.of(WeeklyWellnessRequest.class)
            .set(field(WeeklyWellnessRequest::weekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellnessRequest::perceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessRequest::perceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessRequest::motivation), gen -> gen.ints().range(1, 5))
            .create();
    }

    private WeeklyWellness aValidWellness() {
        return Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
    }

    private void assertJsonMatchesResponse(ResultActions result, WeeklyWellnessResponse response) throws Exception {
        result.andExpect(jsonPath("$.id").value(response.id()))
            .andExpect(jsonPath("$.athleteId").value(response.athleteId()))
            .andExpect(jsonPath("$.weekStartDate").value(response.weekStartDate().toString()))
            .andExpect(jsonPath("$.perceivedDifficulty").value(response.perceivedDifficulty()))
            .andExpect(jsonPath("$.perceivedFatigue").value(response.perceivedFatigue()))
            .andExpect(jsonPath("$.motivation").value(response.motivation()));
    }
}
