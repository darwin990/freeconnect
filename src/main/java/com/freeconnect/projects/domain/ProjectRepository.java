package com.freeconnect.projects.domain;

import com.freeconnect.shared.pagination.PagedResult;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia y busqueda de proyectos.
 */
public interface ProjectRepository {

    Project save(Project project);

    Optional<Project> findById(UUID id);

    PagedResult<Project> search(ProjectSearchCriteria criteria);

    List<Project> findByClient(UUID clientId);

    List<Project> findAll();
}
