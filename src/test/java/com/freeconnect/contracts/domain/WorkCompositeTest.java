package com.freeconnect.contracts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freeconnect.contracts.domain.composite.TaskItem;
import com.freeconnect.contracts.domain.composite.WorkComponent;
import com.freeconnect.shared.domain.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la jerarquia Composite Milestone -&gt; Deliverable -&gt; TaskItem.
 */
class WorkCompositeTest {

    @Test
    void progressIsComputedUniformlyAcrossTheComposite() {
        Deliverable deliverable = Deliverable.submit("API", "Entregable", "https://example.com/repo");
        TaskItem done = deliverable.addTask("Modelo de datos");
        deliverable.addTask("Controladores");
        deliverable.toggleTask(done.id(), true);

        // Propio 60 (SUBMITTED) + tareas 50 -> (60 + 50) / 2
        assertEquals(55, deliverable.progress());
        assertEquals(2, deliverable.children().size());
        assertEquals(2, deliverable.leafCount());

        Milestone milestone = Milestone.rehydrate(UUID.randomUUID(), UUID.randomUUID(), "Hito", "Detalle",
                Money.of(new BigDecimal("500"), "USD"), LocalDate.now().plusDays(10), 0,
                MilestoneStatus.IN_PROGRESS, List.of(deliverable));

        // Propio 30 (IN_PROGRESS) + entregable 55 -> (30 + 55) / 2
        assertEquals(42, milestone.progress());
        assertEquals(2, milestone.leafCount());
    }

    @Test
    void everyNodeExposesTheSameContract() {
        Deliverable deliverable = Deliverable.submit("API", "Entregable", null);
        TaskItem task = deliverable.addTask("Tarea");

        WorkComponent component = task;
        assertEquals(0, component.progress());
        assertTrue(component.children().isEmpty());
        assertEquals(1, component.leafCount());
    }
}
