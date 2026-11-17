package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.Availability;
import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.HourlyRate;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.shared.application.AbstractUseCase;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import com.freeconnect.shared.security.AuthenticatedUser;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso RF-02: crear o actualizar el perfil propio segun el rol.
 */
@Service
public class UpdateMyProfileUseCase extends AbstractUseCase<UpdateProfileCommand, ProfileView> {

    private final ProfileRepository profileRepository;
    private final SkillCatalog skillCatalog;
    private final CurrentUserProvider currentUser;
    private final ProfileViewMapper mapper;

    public UpdateMyProfileUseCase(ProfileRepository profileRepository, SkillCatalog skillCatalog,
                                  CurrentUserProvider currentUser, ProfileViewMapper mapper) {
        this.profileRepository = profileRepository;
        this.skillCatalog = skillCatalog;
        this.currentUser = currentUser;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    protected ProfileView handle(UpdateProfileCommand command) {
        AuthenticatedUser user = currentUser.require();
        if (user.role() == Role.FREELANCER) {
            return mapper.toView(upsertFreelancer(user, command));
        }
        if (user.role() == Role.CLIENT) {
            return mapper.toView(upsertClient(user, command));
        }
        throw new ValidationException("El rol ADMIN no administra perfil publico");
    }

    private FreelancerProfile upsertFreelancer(AuthenticatedUser user, UpdateProfileCommand command) {
        if (command.headline() == null || command.headline().isBlank()) {
            throw new ValidationException("El titular profesional es obligatorio");
        }
        if (command.availability() == null) {
            throw new ValidationException("La disponibilidad es obligatoria");
        }
        Set<Skill> skills = resolveSkills(command.skills());
        FreelancerProfile profile = profileRepository.findFreelancerByUserId(user.id())
                .orElseGet(() -> FreelancerProfile.builder(user.id())
                        .headline(command.headline())
                        .availability(command.availability())
                        .build());
        HourlyRate rate = command.hourlyRate() == null ? null
                : HourlyRate.of(command.hourlyRate(), command.currency() == null ? "USD" : command.currency());
        profile.update(command.headline(), command.bio(), rate,
                command.availability() == null ? Availability.AVAILABLE : command.availability(), skills);
        return profileRepository.save(profile);
    }

    private com.freeconnect.profiles.domain.ClientProfile upsertClient(AuthenticatedUser user,
                                                                       UpdateProfileCommand command) {
        if (command.companyName() == null || command.companyName().isBlank()) {
            throw new ValidationException("El nombre de la empresa es obligatorio");
        }
        var profile = profileRepository.findClientByUserId(user.id())
                .orElseGet(() -> com.freeconnect.profiles.domain.ClientProfile.create(user.id(),
                        command.companyName(), command.description()));
        profile.update(command.companyName(), command.description());
        return profileRepository.save(profile);
    }

    private Set<Skill> resolveSkills(Set<String> names) {
        if (names == null || names.isEmpty()) {
            return Set.of();
        }
        return names.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(skillCatalog::resolve)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
