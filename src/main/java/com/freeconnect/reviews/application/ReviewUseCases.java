package com.freeconnect.reviews.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.ContractStatus;
import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.reviews.domain.Review;
import com.freeconnect.reviews.domain.ReviewRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.event.DomainEventPublisher;
import com.freeconnect.shared.exception.ConflictException;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso de resenas de contrato (RF-06).
 */
@Service
public class ReviewUseCases {

    private final ReviewRepository reviewRepository;
    private final ContractRepository contractRepository;
    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUser;
    private final DomainEventPublisher eventPublisher;

    public ReviewUseCases(ReviewRepository reviewRepository, ContractRepository contractRepository,
                          ProfileRepository profileRepository, CurrentUserProvider currentUser,
                          DomainEventPublisher eventPublisher) {
        this.reviewRepository = reviewRepository;
        this.contractRepository = contractRepository;
        this.profileRepository = profileRepository;
        this.currentUser = currentUser;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ReviewView create(UUID contractId, int rating, String comment) {
        UUID actorId = currentUser.requireUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contrato", contractId));
        currentUser.requireParticipant(contract.clientId(), contract.freelancerId(), "el contrato");
        if (contract.status() != ContractStatus.COMPLETED) {
            throw new DomainException("CONTRACT_NOT_COMPLETED",
                    "Solo se puede resenar un contrato completado");
        }
        if (reviewRepository.existsByContractAndAuthor(contractId, actorId)) {
            throw new ConflictException("Ya existe una resena tuya para este contrato");
        }
        UUID targetId = actorId.equals(contract.clientId()) ? contract.freelancerId() : contract.clientId();
        Review saved = reviewRepository.save(Review.create(contractId, actorId, targetId, rating, comment));
        updateReputation(targetId, rating);
        eventPublisher.publishAll(saved.pullDomainEvents());
        return ReviewView.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewView> byContract(UUID contractId) {
        currentUser.require();
        return reviewRepository.findByContract(contractId).stream().map(ReviewView::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewView> byTarget(UUID userId) {
        return reviewRepository.findByTarget(userId).stream().map(ReviewView::from).toList();
    }

    private void updateReputation(UUID targetId, int rating) {
        Optional<FreelancerProfile> profile = profileRepository.findFreelancerByUserId(targetId);
        profile.ifPresent(freelancer -> {
            int newCount = freelancer.ratingCount() + 1;
            freelancer.updateReputation(freelancer.nextRatingAverage(rating), newCount);
            profileRepository.save(freelancer);
        });
    }
}
