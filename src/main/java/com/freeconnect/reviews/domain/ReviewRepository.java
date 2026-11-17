package com.freeconnect.reviews.domain;

import java.util.List;
import java.util.UUID;

/**
 * Puerto de persistencia de resenas.
 */
public interface ReviewRepository {

    Review save(Review review);

    List<Review> findByContract(UUID contractId);

    List<Review> findByTarget(UUID targetId);

    boolean existsByContractAndAuthor(UUID contractId, UUID authorId);
}
