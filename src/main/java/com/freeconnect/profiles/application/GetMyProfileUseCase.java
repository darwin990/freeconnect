package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.AuthenticatedUser;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: consultar el perfil propio segun el rol del actor.
 */
@Service
public class GetMyProfileUseCase implements UseCase<Void, ProfileView> {

    private final ProfileRepository profileRepository;
    private final ProfileViewMapper mapper;
    private final CurrentUserProvider currentUser;

    public GetMyProfileUseCase(ProfileRepository profileRepository, ProfileViewMapper mapper,
                               CurrentUserProvider currentUser) {
        this.profileRepository = profileRepository;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileView execute(Void input) {
        AuthenticatedUser user = currentUser.require();
        if (user.role() == Role.FREELANCER) {
            return profileRepository.findFreelancerByUserId(user.id())
                    .map(mapper::toView)
                    .orElseThrow(() -> new NotFoundException("Perfil de freelancer", user.id()));
        }
        if (user.role() == Role.CLIENT) {
            return profileRepository.findClientByUserId(user.id())
                    .map(mapper::toView)
                    .orElseThrow(() -> new NotFoundException("Perfil de cliente", user.id()));
        }
        throw new NotFoundException("Los administradores no tienen perfil publico");
    }
}
