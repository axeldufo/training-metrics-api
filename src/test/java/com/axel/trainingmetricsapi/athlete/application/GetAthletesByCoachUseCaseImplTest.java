package com.axel.trainingmetricsapi.athlete.application;

import com.axel.trainingmetricsapi.athlete.domain.Athlete;
import com.axel.trainingmetricsapi.athlete.domain.AthleteRepository;
import com.axel.trainingmetricsapi.shared.domain.PageResult;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAthletesByCoachUseCaseImplTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private GetAthletesByCoachUseCaseImpl useCase;

    @Test
    void execute_shouldReturnAllAthletes_whenRepositoryReturnsThem() {
        long coachId = 4L;
        int pageNumber = 0;
        int pageSize = 20;
        int nbAthletes = 3;
        List<Athlete> athletes = Instancio.ofList(Athlete.class).size(nbAthletes).create();
        when(athleteRepository.findAllByCoachId(coachId, pageNumber, pageSize)).thenReturn(
            new PageResult<>(athletes, nbAthletes, pageNumber, pageSize));

        PageResult<Athlete> result = useCase.execute(coachId, pageNumber, pageSize);

        verify(athleteRepository).findAllByCoachId(coachId, pageNumber, pageSize);
        assertThat(result.content()).isEqualTo(athletes);
        assertThat(result.totalElements()).isEqualTo(nbAthletes);
        assertThat(result.pageNumber()).isEqualTo(pageNumber);
        assertThat(result.pageSize()).isEqualTo(pageSize);
    }
}
