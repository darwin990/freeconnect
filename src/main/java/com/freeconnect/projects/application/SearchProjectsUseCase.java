package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.pagination.PagedResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-03: buscar y filtrar proyectos con paginacion.
 */
@Service
public class SearchProjectsUseCase implements UseCase<SearchProjectsQuery, PagedResult<ProjectView>> {

    private final ProjectRepository projectRepository;

    public SearchProjectsUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<ProjectView> execute(SearchProjectsQuery query) {
        return projectRepository.search(query.toCriteria()).map(ProjectView::from);
    }
}
