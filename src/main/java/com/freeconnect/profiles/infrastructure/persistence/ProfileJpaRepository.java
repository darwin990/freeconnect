package com.freeconnect.profiles.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileJpaRepository extends JpaRepository<ProfileJpaEntity, UUID> {

    Optional<ProfileJpaEntity> findByUserId(UUID userId);

    Optional<ProfileJpaEntity> findByUserIdAndType(UUID userId, String type);
}
