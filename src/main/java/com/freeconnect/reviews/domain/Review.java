package com.freeconnect.reviews.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Resena de un contrato completado (RF-06).
 */
public class Review extends AggregateRoot {

    private final UUID id;
    private final UUID contractId;
    private final UUID authorId;
    private final UUID targetId;
    private final int rating;
    private final String comment;
    private final Instant createdAt;

    private Review(UUID id, UUID contractId, UUID authorId, UUID targetId, int rating, String comment,
                   Instant createdAt) {
        this.id = id;
        this.contractId = contractId;
        this.authorId = authorId;
        this.targetId = targetId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public static Review create(UUID contractId, UUID authorId, UUID targetId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("La calificacion debe estar entre 1 y 5");
        }
        if (comment != null && comment.length() > 2000) {
            throw new ValidationException("El comentario admite hasta 2000 caracteres");
        }
        Review review = new Review(UUID.randomUUID(), contractId, authorId, targetId, rating, comment,
                Instant.now());
        review.registerEvent(new ReviewCreatedEvent(review.id, contractId, authorId, targetId, rating,
                review.createdAt));
        return review;
    }

    public static Review rehydrate(UUID id, UUID contractId, UUID authorId, UUID targetId, int rating,
                                   String comment, Instant createdAt) {
        return new Review(id, contractId, authorId, targetId, rating, comment, createdAt);
    }

    public UUID id() {
        return id;
    }

    public UUID contractId() {
        return contractId;
    }

    public UUID authorId() {
        return authorId;
    }

    public UUID targetId() {
        return targetId;
    }

    public int rating() {
        return rating;
    }

    public String comment() {
        return comment;
    }

    public Instant createdAt() {
        return createdAt;
    }
}
