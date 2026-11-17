package com.freeconnect.reviews.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/**
 * Modelo JPA de resena.
 */
@Entity
@Table(name = "reviews")
public class ReviewJpaEntity {

    @Id
    private UUID id;

    @Column(name = "contract_id", nullable = false)
    private UUID contractId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected ReviewJpaEntity() {
    }

    public ReviewJpaEntity(UUID id, UUID contractId, UUID authorId, UUID targetId, int rating, String comment,
                           Instant createdAt) {
        this.id = id;
        this.contractId = contractId;
        this.authorId = authorId;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getContractId() {
        return contractId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
