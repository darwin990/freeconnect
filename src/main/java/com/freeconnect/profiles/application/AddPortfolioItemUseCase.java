package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.PortfolioItem;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.security.CurrentUserProvider;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: agregar evidencia al portafolio.
 */
@Service
public class AddPortfolioItemUseCase extends AbstractUseCase<AddPortfolioItemUseCase.Command, ProfileView> {

    private final ProfileRepository profileRepository;
    private final CurrentUserProvider currentUser;
    private final ProfileViewMapper mapper;

    public AddPortfolioItemUseCase(ProfileRepository profileRepository, CurrentUserProvider currentUser,
                                   ProfileViewMapper mapper) {
        this.profileRepository = profileRepository;
        this.currentUser = currentUser;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    protected ProfileView handle(Command command) {
        UUID userId = currentUser.requireUserId();
        FreelancerProfile profile = profileRepository.findFreelancerByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Perfil de freelancer", userId));
        profile.addPortfolioItem(command.title(), command.url(), command.description());
        return mapper.toView(profileRepository.save(profile));
    }

    public record Command(String title, String url, String description) {
    }
}
