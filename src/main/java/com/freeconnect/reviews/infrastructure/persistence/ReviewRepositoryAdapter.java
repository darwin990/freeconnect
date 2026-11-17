package com.freeconnect.reviews.infrastructure.persistence;

import com.freeconnect.reviews.domain.Review;
import com.freeconnect.reviews.domain.ReviewRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA de resenas.
 */
@Repository
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewJpaRepository repository;

    public ReviewRepositoryAdapter(ReviewJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Review save(Review review) {
        repository.save(new ReviewJpaEntity(review.id(), review.contractId(), review.authorId(),
                review.targetId(), review.rating(), review.comment(), review.createdAt()));
        return review;
    }

    @Override
    public List<Review> findByContract(UUID contractId) {
        return repository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Review> findByTarget(UUID targetId) {
        return repository.findByTargetIdOrderByCreatedAtDesc(targetId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsByContractAndAuthor(UUID contractId, UUID authorId) {
        return repository.existsByContractIdAndAuthorId(contractId, authorId);
    }

    private Review toDomain(ReviewJpaEntity entity) {
        return Review.rehydrate(entity.getId(), entity.getContractId(), entity.getAuthorId(),
                entity.getTargetId(), entity.getRating(), entity.getComment(), entity.getCreatedAt());
    }
}
