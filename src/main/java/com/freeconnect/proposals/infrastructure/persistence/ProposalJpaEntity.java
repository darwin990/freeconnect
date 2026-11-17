package com.freeconnect.proposals.infrastructure.persistence;

import com.freeconnect.proposals.domain.ProposalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo JPA de propuesta.
 */
@Entity
@Table(name = "proposals")
public class ProposalJpaEntity {

    @Id
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "freelancer_id", nullable = false)
    private UUID freelancerId;

    @Column(name = "cover_letter", nullable = false, length = 3000)
    private String coverLetter;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "estimated_days", nullable = false)
    private int estimatedDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status;

    @Column(name = "evaluation_score")
    private Integer evaluationScore;

    @Column(name = "evaluation_note", length = 1000)
    private String evaluationNote;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ProposalJpaEntity() {
    }

    public ProposalJpaEntity(UUID id, UUID projectId, UUID freelancerId, String coverLetter, BigDecimal amount,
                             String currency, int estimatedDays, ProposalStatus status, Integer evaluationScore,
                             String evaluationNote, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.freelancerId = freelancerId;
        this.coverLetter = coverLetter;
        this.amount = amount;
        this.currency = currency;
        this.estimatedDays = estimatedDays;
        this.status = status;
        this.evaluationScore = evaluationScore;
        this.evaluationNote = evaluationNote;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public UUID getFreelancerId() {
        return freelancerId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public int getEstimatedDays() {
        return estimatedDays;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public Integer getEvaluationScore() {
        return evaluationScore;
    }

    public String getEvaluationNote() {
        return evaluationNote;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public void setEvaluationScore(Integer evaluationScore) {
        this.evaluationScore = evaluationScore;
    }

    public void setEvaluationNote(String evaluationNote) {
        this.evaluationNote = evaluationNote;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
