package com.freeconnect.projects.domain.state;

import com.freeconnect.projects.domain.ProjectStatus;
import java.util.Map;
import java.util.Set;

/**
 * Implementaciones concretas y registro de estados.
 *
 * <p><b>Patron: State.</b> El mapa es inmutable y sustituye cualquier logica
 * condicional de transiciones.</p>
 */
public final class ProjectStates {

    private static final ProjectState DRAFT = new SimpleState(ProjectStatus.DRAFT,
            Set.of(ProjectStatus.PUBLISHED, ProjectStatus.CANCELLED));
    private static final ProjectState PUBLISHED = new SimpleState(ProjectStatus.PUBLISHED,
            Set.of(ProjectStatus.PAUSED, ProjectStatus.IN_PROGRESS, ProjectStatus.CANCELLED));
    private static final ProjectState PAUSED = new SimpleState(ProjectStatus.PAUSED,
            Set.of(ProjectStatus.PUBLISHED, ProjectStatus.CANCELLED));
    private static final ProjectState IN_PROGRESS = new SimpleState(ProjectStatus.IN_PROGRESS,
            Set.of(ProjectStatus.FINISHED, ProjectStatus.CANCELLED));
    private static final ProjectState FINISHED = new SimpleState(ProjectStatus.FINISHED, Set.of());
    private static final ProjectState CANCELLED = new SimpleState(ProjectStatus.CANCELLED, Set.of());

    private static final Map<ProjectStatus, ProjectState> REGISTRY = Map.of(
            ProjectStatus.DRAFT, DRAFT,
            ProjectStatus.PUBLISHED, PUBLISHED,
            ProjectStatus.PAUSED, PAUSED,
            ProjectStatus.IN_PROGRESS, IN_PROGRESS,
            ProjectStatus.FINISHED, FINISHED,
            ProjectStatus.CANCELLED, CANCELLED);

    private ProjectStates() {
    }

    public static ProjectState of(ProjectStatus status) {
        return REGISTRY.get(status);
    }

    private record SimpleState(ProjectStatus status, Set<ProjectStatus> allowedTransitions) implements ProjectState {
    }
}
