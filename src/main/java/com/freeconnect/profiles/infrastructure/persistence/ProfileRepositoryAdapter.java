package com.freeconnect.profiles.infrastructure.persistence;

import com.freeconnect.profiles.domain.Availability;
import com.freeconnect.profiles.domain.ClientProfile;
import com.freeconnect.profiles.domain.FreelancerProfile;
import com.freeconnect.profiles.domain.HourlyRate;
import com.freeconnect.profiles.domain.PortfolioItem;
import com.freeconnect.profiles.domain.ProfileRepository;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.profiles.domain.SkillRepository;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA que traduce entre agregados de dominio y entidades persistidas.
 */
@Repository
public class ProfileRepositoryAdapter implements ProfileRepository {

    private static final String FREELANCER = "FREELANCER";
    private static final String CLIENT = "CLIENT";

    private final ProfileJpaRepository profiles;
    private final PortfolioItemJpaRepository portfolioItems;
    private final SkillRepository skills;

    public ProfileRepositoryAdapter(ProfileJpaRepository profiles, PortfolioItemJpaRepository portfolioItems,
                                    SkillRepository skills) {
        this.profiles = profiles;
        this.portfolioItems = portfolioItems;
        this.skills = skills;
    }

    @Override
    public FreelancerProfile save(FreelancerProfile profile) {
        ProfileJpaEntity entity = profiles.findById(profile.id())
                .orElseGet(() -> new ProfileJpaEntity(profile.id(), profile.userId(), FREELANCER, profile.headline(),
                        profile.bio(), rate(profile), currency(profile), availability(profile), null, null,
                        profile.ratingAverage(), profile.ratingCount(), profile.createdAt(), profile.updatedAt()));
        entity.setHeadline(profile.headline());
        entity.setBio(profile.bio());
        entity.setHourlyRate(rate(profile));
        entity.setCurrency(currency(profile));
        entity.setAvailability(availability(profile));
        entity.setRatingAverage(profile.ratingAverage());
        entity.setRatingCount(profile.ratingCount());
        entity.setUpdatedAt(profile.updatedAt());
        entity.replaceSkillIds(profile.skills().stream().map(Skill::id).collect(Collectors.toCollection(LinkedHashSet::new)));
        profiles.save(entity);
        syncPortfolio(profile.id(), profile.portfolio());
        return profile;
    }

    @Override
    public ClientProfile save(ClientProfile profile) {
        ProfileJpaEntity entity = profiles.findById(profile.id())
                .orElseGet(() -> new ProfileJpaEntity(profile.id(), profile.userId(), CLIENT, null, null, null,
                        null, null, profile.companyName(), profile.description(), null, 0, profile.createdAt(),
                        profile.updatedAt()));
        entity.setCompanyName(profile.companyName());
        entity.setDescription(profile.description());
        entity.setUpdatedAt(profile.updatedAt());
        profiles.save(entity);
        return profile;
    }

    @Override
    public Optional<FreelancerProfile> findFreelancerByUserId(UUID userId) {
        return profiles.findByUserIdAndType(userId, FREELANCER).map(this::toFreelancer);
    }

    @Override
    public Optional<ClientProfile> findClientByUserId(UUID userId) {
        return profiles.findByUserIdAndType(userId, CLIENT).map(this::toClient);
    }

    @Override
    public Optional<FreelancerProfile> findFreelancerById(UUID profileId) {
        return profiles.findById(profileId).filter(e -> FREELANCER.equals(e.getType())).map(this::toFreelancer);
    }

    @Override
    public Optional<ClientProfile> findClientById(UUID profileId) {
        return profiles.findById(profileId).filter(e -> CLIENT.equals(e.getType())).map(this::toClient);
    }

    private void syncPortfolio(UUID profileId, List<PortfolioItem> items) {
        List<PortfolioItemJpaEntity> existing = portfolioItems.findByProfileId(profileId);
        Set<UUID> existingIds = existing.stream().map(PortfolioItemJpaEntity::getId).collect(Collectors.toSet());
        for (PortfolioItem item : items) {
            if (existingIds.contains(item.id())) {
                existing.stream().filter(e -> e.getId().equals(item.id())).findFirst()
                        .ifPresent(e -> {
                            e.setUrl(item.url());
                            e.setStorageKey(item.storageKey());
                            portfolioItems.save(e);
                        });
            } else {
                portfolioItems.save(new PortfolioItemJpaEntity(item.id(), profileId, item.title(), item.url(),
                        item.description(), item.storageKey()));
            }
        }
    }

    private FreelancerProfile toFreelancer(ProfileJpaEntity entity) {
        Set<Skill> profileSkills = entity.getSkillIds().stream()
                .map(skills::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<PortfolioItem> portfolio = portfolioItems.findByProfileId(entity.getId()).stream()
                .map(i -> new PortfolioItem(i.getId(), i.getTitle(), i.getUrl(), i.getDescription(),
                        i.getStorageKey()))
                .toList();
        FreelancerProfile.Builder builder = FreelancerProfile.builder(entity.getUserId())
                .id(entity.getId())
                .headline(entity.getHeadline())
                .bio(entity.getBio())
                .availability(entity.getAvailability() == null ? Availability.AVAILABLE
                        : Availability.valueOf(entity.getAvailability()))
                .skills(profileSkills)
                .portfolio(portfolio)
                .rating(entity.getRatingAverage(), entity.getRatingCount())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt());
        if (entity.getHourlyRate() != null && entity.getCurrency() != null) {
            builder.hourlyRate(HourlyRate.of(entity.getHourlyRate(), entity.getCurrency()));
        }
        return builder.build();
    }

    private ClientProfile toClient(ProfileJpaEntity entity) {
        return ClientProfile.rehydrate(entity.getId(), entity.getUserId(), entity.getCompanyName(),
                entity.getDescription(), entity.getCreatedAt(), entity.getUpdatedAt());
    }

    private BigDecimal rate(FreelancerProfile profile) {
        return profile.hourlyRate() == null ? null : profile.hourlyRate().amount();
    }

    private String currency(FreelancerProfile profile) {
        return profile.hourlyRate() == null ? null : profile.hourlyRate().currency();
    }

    private String availability(FreelancerProfile profile) {
        return profile.availability().name();
    }
}
