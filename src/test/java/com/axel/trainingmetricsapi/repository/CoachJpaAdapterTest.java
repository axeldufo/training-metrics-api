package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.Coach;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachJpaAdapterTest {

    @Mock
    private CoachJpaRepository coachJpaRepository;

    @Mock
    private CoachPersistenceMapper coachPersistenceMapper;

    @InjectMocks
    private CoachJpaAdapter coachJpaAdapter;

    @Test
    void findById_shouldReturnMappedCoach() {
        long persistedId = 42L;
        CoachJpaEntity persistedCoachEntity = Instancio.create(CoachJpaEntity.class);
        persistedCoachEntity.setId(persistedId);
        Coach expectedCoach = Instancio.create(Coach.class);
        expectedCoach.setId(persistedId);

        when(coachJpaRepository.findById(persistedId)).thenReturn(Optional.of(persistedCoachEntity));
        when(coachPersistenceMapper.entityToDomain(persistedCoachEntity)).thenReturn(expectedCoach);

        Optional<Coach> coachFound = coachJpaAdapter.findById(persistedId);

        verify(coachJpaRepository).findById(persistedId);
        verify(coachPersistenceMapper).entityToDomain(persistedCoachEntity);
        assertThat(coachFound).contains(expectedCoach);
        assertThat(coachFound.get().getId()).isEqualTo(persistedId);  // id is excluded from Coach.isEqualTo()
    }

    @Test
    void findById_shouldBeEmptyIfNotFound() {
        Optional<Coach> coachFound = coachJpaAdapter.findById(18L);

        assertThat(coachFound).isEmpty();
    }

    @Test
    void deleteById_shouldDeleteIfExists() {
        long coachId = 4L;

        coachJpaAdapter.deleteById(coachId);

        verify(coachJpaRepository).deleteById(coachId);
    }

    @Test
    void existsById_shouldReturnTrue_whenExists() {
        long coachId = 4L;
        when(coachJpaRepository.existsById(coachId)).thenReturn(true);

        assertThat(coachJpaAdapter.existsById(coachId)).isTrue();

        verify(coachJpaRepository).existsById(coachId);
    }

    @Test
    void existsById_shouldReturnFalse_whenDoesntExist() {
        long coachId = 4L;
        when(coachJpaRepository.existsById(coachId)).thenReturn(false);

        assertThat(coachJpaAdapter.existsById(coachId)).isFalse();

        verify(coachJpaRepository).existsById(coachId);
    }

    @Test
    void updateName_should() {
        long id = 4L;
        String name = "Alice Dupont";

        coachJpaAdapter.updateName(id, name);

        verify(coachJpaRepository).updateName(id, name);
    }

}
