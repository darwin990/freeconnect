package com.freeconnect.profiles.application;

import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.profiles.domain.ClientProfile;
import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.HourlyRate;
import com.freeconnect.profiles.domain.PortfolioItem;
import com.freeconnect.profiles.domain.Skill;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Traduce agregados de perfil a DTOs de aplicacion.
 */
@Component
public class ProfileViewMapper {

    private final UserRepository userRepository;

    public ProfileViewMapper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ProfileView toView(FreelancerProfile profile) {
        HourlyRate rate = profile.hourlyRate();
        return new ProfileView(profile.id(), profile.userId(), "FREELANCER", displayName(profile.userId()),
                profile.headline(), profile.bio(), rate == null ? null : rate.amount(),
                rate == null ? null : rate.currency(), profile.availability(), null, null,
                profile.skills().stream().map(this::toSkillView).toList(),
                profile.portfolio().stream().map(this::toPortfolioView).toList(),
                profile.ratingAverage(), profile.ratingCount());
    }

    public ProfileView toView(ClientProfile profile) {
        return new ProfileView(profile.id(), profile.userId(), "CLIENT", displayName(profile.userId()), null,
                null, null, null, null, profile.companyName(), profile.description(), List.of(), List.of(),
                null, 0);
    }

    private ProfileView.SkillView toSkillView(Skill skill) {
        return new ProfileView.SkillView(skill.id(), skill.name(), skill.category());
    }

    private ProfileView.PortfolioItemView toPortfolioView(PortfolioItem item) {
        return new ProfileView.PortfolioItemView(item.id(), item.title(), item.url(), item.description());
    }

    private String displayName(UUID userId) {
        return userRepository.findById(userId).map(user -> user.displayName()).orElse(null);
    }
}
