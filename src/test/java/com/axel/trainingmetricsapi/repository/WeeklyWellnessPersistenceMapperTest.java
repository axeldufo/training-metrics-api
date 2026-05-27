package com.axel.trainingmetricsapi.repository;

import com.axel.trainingmetricsapi.domain.WeeklyWellness;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class WeeklyWellnessPersistenceMapperTest {

    private final WeeklyWellnessPersistenceMapper mapper = new WeeklyWellnessPersistenceMapper();

    @Test
    void domainToEntity_shouldMapAllFields() {
        WeeklyWellness wellness = Instancio.of(WeeklyWellness.class)
            .set(field(WeeklyWellness::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellness::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellness::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        wellness.setId(null);

        WeeklyWellnessJpaEntity entity = mapper.domainToEntity(wellness);

        assertThat(entity.getId()).isNull();
        assertThat(entity.getWeekStartDate()).isEqualTo(wellness.getWeekStartDate());
        assertThat(entity.getPerceivedDifficulty()).isEqualTo(wellness.getPerceivedDifficulty());
        assertThat(entity.getPerceivedFatigue()).isEqualTo(wellness.getPerceivedFatigue());
        assertThat(entity.getMotivation()).isEqualTo(wellness.getMotivation());
        assertThat(entity.getAthlete().getId()).isEqualTo(wellness.getAthleteId());
    }

    @Test
    void entityToDomain_shouldMapAllFields() {
        WeeklyWellnessJpaEntity entity = Instancio.of(WeeklyWellnessJpaEntity.class)
            .set(field(WeeklyWellnessJpaEntity::getWeekStartDate), LocalDate.now().with(DayOfWeek.MONDAY))
            .generate(field(WeeklyWellnessJpaEntity::getPerceivedDifficulty), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessJpaEntity::getPerceivedFatigue), gen -> gen.ints().range(1, 5))
            .generate(field(WeeklyWellnessJpaEntity::getMotivation), gen -> gen.ints().range(1, 5))
            .create();
        entity.setId(88L);

        WeeklyWellness wellness = mapper.entityToDomain(entity);

        assertThat(wellness.getId()).isEqualTo(88L);
        assertThat(wellness.getAthleteId()).isEqualTo(entity.getAthlete().getId());
        assertThat(wellness.getWeekStartDate()).isEqualTo(entity.getWeekStartDate());
        assertThat(wellness.getPerceivedDifficulty()).isEqualTo(entity.getPerceivedDifficulty());
        assertThat(wellness.getPerceivedFatigue()).isEqualTo(entity.getPerceivedFatigue());
        assertThat(wellness.getMotivation()).isEqualTo(entity.getMotivation());
    }
}
