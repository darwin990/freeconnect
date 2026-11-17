package com.freeconnect.projects.domain.state;

import com.freeconnect.projects.domain.ProjectStatus;
import java.util.Set;

/**
 * Estado del ciclo de vida de un proyecto.
 *
 * <p><b>Patron: State.</b> Cada estado encapsula sus transiciones validas; el
 * agregado delega en el estado actual y no usa cadenas de {@code if}.</p>
 */
public interface ProjectState {

    ProjectStatus status();

    Set<ProjectStatus> allowedTransitions();

    default boolean canTransitionTo(ProjectStatus target) {
        return allowedTransitions().contains(target);
    }

    /** Hook de entrada al estado (extensible). */
    default void onEnter(ProjectStatus previous) {
        // sin efectos por defecto
    }
}
