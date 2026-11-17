package com.freeconnect.profiles.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia de perfiles.
 */
public interface ProfileRepository {

    FreelancerProfile save(FreelancerProfile profile);

    ClientProfile save(ClientProfile profile);

    Optional<FreelancerProfile> findFreelancerByUserId(UUID userId);

    Optional<ClientProfile> findClientByUserId(UUID userId);

    Optional<FreelancerProfile> findFreelancerById(UUID profileId);

    Optional<ClientProfile> findClientById(UUID profileId);
}
