package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: consulta publica de perfil por identificador.
 */
@Service
public class GetProfileByIdUseCase implements UseCase<UUID, ProfileView> {

    private final ProfileRepository profileRepository;
    private final ProfileViewMapper mapper;

    public GetProfileByIdUseCase(ProfileRepository profileRepository, ProfileViewMapper mapper) {
        this.profileRepository = profileRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileView execute(UUID profileId) {
        return profileRepository.findFreelancerById(profileId)
                .map(mapper::toView)
                .or(() -> profileRepository.findClientById(profileId).map(mapper::toView))
                .orElseThrow(() -> new NotFoundException("Perfil", profileId));
    }
}
