package com.freeconnect.contracts.domain.composite;

import java.util.List;
import java.util.UUID;

/**
 * Hoja del Composite: tarea concreta marcable como completada.
 */
public class TaskItem implements WorkComponent {

    private final UUID id;
    private final String title;
    private boolean done;

    public TaskItem(UUID id, String title, boolean done) {
        this.id = id;
        this.title = title;
        this.done = done;
    }

    public static TaskItem create(String title) {
        return new TaskItem(UUID.randomUUID(), title, false);
    }

    public void toggle(boolean completed) {
        this.done = completed;
    }

    @Override
    public UUID id() {
        return id;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public List<WorkComponent> children() {
        return List.of();
    }

    @Override
    public int ownProgress() {
        return done ? 100 : 0;
    }

    public boolean isDone() {
        return done;
    }
}
