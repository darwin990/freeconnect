package com.freeconnect.contracts.domain;

import com.freeconnect.contracts.domain.composite.TaskItem;
import com.freeconnect.contracts.domain.composite.WorkComponent;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Entregable de un hito.
 *
 * <p><b>Patron: Composite (nodo intermedio).</b> Agrupa {@link TaskItem} y expone
 * el mismo contrato {@link WorkComponent} que el hito y las tareas.</p>
 */
public class Deliverable implements WorkComponent {

    private final UUID id;
    private final String title;
    private final String description;
    private String url;
    private DeliverableStatus status;
    private final Instant submittedAt;
    private final List<TaskItem> tasks;
    private final List<DeliverableComment> comments;

    private Deliverable(UUID id, String title, String description, String url, DeliverableStatus status,
                        Instant submittedAt, List<TaskItem> tasks, List<DeliverableComment> comments) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.url = url;
        this.status = status;
        this.submittedAt = submittedAt;
        this.tasks = new ArrayList<>(tasks);
        this.comments = new ArrayList<>(comments);
    }

    public static Deliverable submit(String title, String description, String url) {
        if (title == null || title.isBlank()) {
            throw new ValidationException("El titulo del entregable es obligatorio");
        }
        return new Deliverable(UUID.randomUUID(), title.trim(), description, url, DeliverableStatus.SUBMITTED,
                Instant.now(), List.of(), List.of());
    }

    public static Deliverable rehydrate(UUID id, String title, String description, String url,
                                        DeliverableStatus status, Instant submittedAt, List<TaskItem> tasks,
                                        List<DeliverableComment> comments) {
        return new Deliverable(id, title, description, url, status, submittedAt, tasks, comments);
    }

    public TaskItem addTask(String taskTitle) {
        if (taskTitle == null || taskTitle.isBlank()) {
            throw new ValidationException("El titulo de la tarea es obligatorio");
        }
        TaskItem task = TaskItem.create(taskTitle.trim());
        tasks.add(task);
        return task;
    }

    public void toggleTask(UUID taskId, boolean done) {
        tasks.stream().filter(task -> task.id().equals(taskId)).findFirst()
                .orElseThrow(() -> new ValidationException("Tarea no encontrada en el entregable"))
                .toggle(done);
    }

    public DeliverableComment addComment(UUID authorId, String body) {
        if (body == null || body.isBlank()) {
            throw new ValidationException("El comentario no puede estar vacio");
        }
        DeliverableComment comment = DeliverableComment.of(authorId, body.trim());
        comments.add(comment);
        return comment;
    }

    public void approve() {
        this.status = DeliverableStatus.APPROVED;
        tasks.forEach(task -> task.toggle(true));
    }

    public void reject() {
        this.status = DeliverableStatus.REJECTED;
    }

    public void attachUrl(String newUrl) {
        this.url = newUrl;
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
        return List.copyOf(tasks);
    }

    @Override
    public int ownProgress() {
        return switch (status) {
            case APPROVED -> 100;
            case REJECTED -> 0;
            case SUBMITTED -> 60;
            default -> 20;
        };
    }

    @Override
    public int progress() {
        if (tasks.isEmpty()) {
            return ownProgress();
        }
        List<WorkComponent> base = new ArrayList<>(tasks);
        int taskProgress = base.stream().mapToInt(WorkComponent::progress).sum() / base.size();
        return (taskProgress + ownProgress()) / 2;
    }

    public String description() {
        return description;
    }

    public String url() {
        return url;
    }

    public DeliverableStatus status() {
        return status;
    }

    public Instant submittedAt() {
        return submittedAt;
    }

    public List<TaskItem> tasks() {
        return Collections.unmodifiableList(tasks);
    }

    public List<DeliverableComment> comments() {
        return Collections.unmodifiableList(comments);
    }
}
