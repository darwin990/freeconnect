package com.freeconnect.projects.infrastructure.persistence;

import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Modelo JPA de proyecto.
 */
@Entity
@Table(name = "projects")
public class ProjectJpaEntity {

    @Id
    private UUID id;

    @Column(name = "client_id", nullable = false)
    private UUID clientId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProjectCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modality modality;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    @Column(name = "budget_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal budgetAmount;

    @Column(name = "budget_currency", nullable = false, length = 3)
    private String budgetCurrency;

    private LocalDate deadline;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "project_skills", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "skill_id", nullable = false)
    private Set<UUID> skillIds = new LinkedHashSet<>();

    protected ProjectJpaEntity() {
    }

    public ProjectJpaEntity(UUID id, UUID clientId, String title, String description, ProjectCategory category,
                            Modality modality, ProjectStatus status, BigDecimal budgetAmount, String budgetCurrency,
                            LocalDate deadline, Instant publishedAt, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.clientId = clientId;
        this.title = title;
        this.description = description;
        this.category = category;
        this.modality = modality;
        this.status = status;
        this.budgetAmount = budgetAmount;
        this.budgetCurrency = budgetCurrency;
        this.deadline = deadline;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public ProjectCategory getCategory() {
        return category;
    }

    public Modality getModality() {
        return modality;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public String getBudgetCurrency() {
        return budgetCurrency;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<UUID> getSkillIds() {
        return skillIds;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(ProjectCategory category) {
        this.category = category;
    }

    public void setModality(Modality modality) {
        this.modality = modality;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void setBudgetCurrency(String budgetCurrency) {
        this.budgetCurrency = budgetCurrency;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void replaceSkillIds(Set<UUID> ids) {
        this.skillIds.clear();
        this.skillIds.addAll(ids);
    }
}
