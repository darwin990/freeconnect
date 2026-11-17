package com.freeconnect.contracts.infrastructure.persistence;

import com.freeconnect.contracts.domain.DeliverableStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Modelo JPA de entregable.
 */
@Entity
@Table(name = "deliverables")
public class DeliverableJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(length = 4000)
    private String description;

    @Column(length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DeliverableStatus status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private List<TaskItemJpaEntity> tasks = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "deliverable_id", nullable = false)
    private List<DeliverableCommentJpaEntity> comments = new ArrayList<>();

    protected DeliverableJpaEntity() {
    }

    public DeliverableJpaEntity(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DeliverableStatus getStatus() {
        return status;
    }

    public void setStatus(DeliverableStatus status) {
        this.status = status;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }

    public List<TaskItemJpaEntity> getTasks() {
        return tasks;
    }

    public List<DeliverableCommentJpaEntity> getComments() {
        return comments;
    }
}
