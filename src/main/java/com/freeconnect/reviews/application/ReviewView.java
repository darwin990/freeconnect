package com.freeconnect.reviews.application;

import com.freeconnect.reviews.domain.Review;
import java.time.Instant;
import java.util.UUID;

/**
 * Vista de resena.
 */
public record ReviewView(UUID id, UUID contractId, UUID authorId, UUID targetId, int rating, String comment,
                         Instant createdAt) {

    public static ReviewView from(Review review) {
        return new ReviewView(review.id(), review.contractId(), review.authorId(), review.targetId(),
                review.rating(), review.comment(), review.createdAt());
    }
}
