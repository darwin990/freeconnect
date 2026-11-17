package com.freeconnect.contracts.domain.composite;

import java.util.List;
import java.util.UUID;

/**
 * Componente comun de la jerarquia Milestone -&gt; Deliverable -&gt; TaskItem.
 *
 * <p><b>Patron: Composite.</b> Permite calcular progreso y recorrer la estructura
 * de trabajo de forma uniforme, sin distinguir hojas de compuestos.</p>
 */
public interface WorkComponent {

    UUID id();

    String title();

    List<WorkComponent> children();

    /** Progreso propio en porcentaje (0-100). */
    int ownProgress();

    default int progress() {
        List<WorkComponent> children = children();
        if (children.isEmpty()) {
            return ownProgress();
        }
        int total = children.stream().mapToInt(WorkComponent::progress).sum();
        return total / children.size();
    }

    default int leafCount() {
        List<WorkComponent> children = children();
        if (children.isEmpty()) {
            return 1;
        }
        return children.stream().mapToInt(WorkComponent::leafCount).sum();
    }
}
