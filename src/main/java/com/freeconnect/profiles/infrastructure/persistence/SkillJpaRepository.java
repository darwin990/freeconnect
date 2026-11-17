package com.freeconnect.profiles.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillJpaRepository extends JpaRepository<SkillJpaEntity, UUID> {

    Optional<SkillJpaEntity> findByNameIgnoreCase(String name);
}
