package com.freeconnect.profiles.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioItemJpaRepository extends JpaRepository<PortfolioItemJpaEntity, UUID> {

    List<PortfolioItemJpaEntity> findByProfileId(UUID profileId);

    Optional<PortfolioItemJpaEntity> findByIdAndProfileId(UUID id, UUID profileId);
}
