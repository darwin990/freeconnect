package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.Availability;
import com.freeconnect.profiles.domain.PortfolioItem;
import com.freeconnect.profiles.domain.Skill;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Vista publica de perfil (DTO de aplicacion, sin entidades JPA).
 */
public record ProfileView(UUID id, UUID userId, String type, String displayName, String headline, String bio,
                          BigDecimal hourlyRate, String currency, Availability availability, String companyName,
                          String description, List<SkillView> skills, List<PortfolioItemView> portfolio,
                          BigDecimal ratingAverage, int ratingCount) {

    public record SkillView(UUID id, String name, String category) {
        static SkillView from(Skill skill) {
            return new SkillView(skill.id(), skill.name(), skill.category());
        }
    }

    public record PortfolioItemView(UUID id, String title, String url, String description) {
        static PortfolioItemView from(PortfolioItem item) {
            return new PortfolioItemView(item.id(), item.title(), item.url(), item.description());
        }
    }
}
