package com.axel.trainingmetricsapi.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoachJpaRepository extends JpaRepository<CoachJpaEntity, Long> {
    boolean existsByEmail(String email);
    Optional<CoachJpaEntity> findByEmail(String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE CoachJpaEntity c SET c.name = :name WHERE c.id = :id")
    void updateName(@Param("id") long id, @Param("name") String name);
}
