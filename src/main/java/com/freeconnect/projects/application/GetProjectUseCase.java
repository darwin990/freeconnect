package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: consultar el detalle de un proyecto.
 */
@Service
public class GetProjectUseCase implements UseCase<UUID, ProjectView> {

    private final ProjectRepository projectRepository;

    public GetProjectUseCase(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectView execute(UUID projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectView::from)
                .orElseThrow(() -> new NotFoundException("Proyecto", projectId));
    }
}
