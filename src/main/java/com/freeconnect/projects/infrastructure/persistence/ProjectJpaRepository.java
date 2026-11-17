package com.freeconnect.projects.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProjectJpaRepository extends JpaRepository<ProjectJpaEntity, UUID>,
        JpaSpecificationExecutor<ProjectJpaEntity> {

    List<ProjectJpaEntity> findByClientIdOrderByCreatedAtDesc(UUID clientId);

    long countByStatus(com.freeconnect.projects.domain.ProjectStatus status);

    List<ProjectJpaEntity> findByCreatedAtAfter(Instant after);
}
