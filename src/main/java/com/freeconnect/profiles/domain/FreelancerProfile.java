package com.freeconnect.profiles.domain;

import com.freeconnect.shared.domain.AggregateRoot;
import com.freeconnect.shared.exception.NotFoundException;
import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Agregado de perfil profesional (RF-02).
 *
 * <p><b>Patron: Builder.</b> La construccion valida invariantes (headline,
 * disponibilidad, tarifa) antes de exponer una instancia consistente.</p>
 */
public class FreelancerProfile extends AggregateRoot {

    private final UUID id;
    private final UUID userId;
    private String headline;
    private String bio;
    private HourlyRate hourlyRate;
    private Availability availability;
    private final Set<Skill> skills;
    private final List<PortfolioItem> portfolio;
    private BigDecimal ratingAverage;
    private int ratingCount;
    private final Instant createdAt;
    private Instant updatedAt;

    private FreelancerProfile(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.headline = builder.headline;
        this.bio = builder.bio;
        this.hourlyRate = builder.hourlyRate;
        this.availability = builder.availability;
        this.skills = new LinkedHashSet<>(builder.skills);
        this.portfolio = new java.util.ArrayList<>(builder.portfolio);
        this.ratingAverage = builder.ratingAverage;
        this.ratingCount = builder.ratingCount;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder(UUID userId) {
        return new Builder(userId);
    }

    public void update(String headline, String bio, HourlyRate hourlyRate, Availability availability,
                       Set<Skill> newSkills) {
        if (headline == null || headline.isBlank()) {
            throw new ValidationException("El titular profesional es obligatorio");
        }
        if (availability == null) {
            throw new ValidationException("La disponibilidad es obligatoria");
        }
        this.headline = headline.trim();
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.availability = availability;
        this.skills.clear();
        this.skills.addAll(newSkills);
        this.updatedAt = Instant.now();
    }

    public PortfolioItem addPortfolioItem(String title, String url, String description) {
        PortfolioItem item = PortfolioItem.create(title, url, description);
        if (portfolio.size() >= 20) {
            throw new ValidationException("El portafolio admite como maximo 20 elementos");
        }
        portfolio.add(item);
        this.updatedAt = Instant.now();
        return item;
    }

    public PortfolioItem attachPortfolioUrl(UUID itemId, String url) {
        for (int i = 0; i < portfolio.size(); i++) {
            if (portfolio.get(i).id().equals(itemId)) {
                PortfolioItem updated = portfolio.get(i).withUrl(url);
                portfolio.set(i, updated);
                this.updatedAt = Instant.now();
                return updated;
            }
        }
        throw new NotFoundException("Elemento de portafolio", itemId);
    }

    public PortfolioItem attachPortfolioFile(UUID itemId, String url, String storageKey) {
        for (int i = 0; i < portfolio.size(); i++) {
            if (portfolio.get(i).id().equals(itemId)) {
                PortfolioItem updated = portfolio.get(i).withStorage(url, storageKey);
                portfolio.set(i, updated);
                this.updatedAt = Instant.now();
                return updated;
            }
        }
        throw new NotFoundException("Elemento de portafolio", itemId);
    }

    public void updateReputation(BigDecimal newAverage, int newCount) {
        this.ratingAverage = newAverage;
        this.ratingCount = newCount;
        this.updatedAt = Instant.now();
    }

    public BigDecimal nextRatingAverage(int newRating) {
        BigDecimal total = ratingAverage.multiply(BigDecimal.valueOf(ratingCount))
                .add(BigDecimal.valueOf(newRating));
        return total.divide(BigDecimal.valueOf(ratingCount + 1L), 2, java.math.RoundingMode.HALF_UP);
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String headline() {
        return headline;
    }

    public String bio() {
        return bio;
    }

    public HourlyRate hourlyRate() {
        return hourlyRate;
    }

    public Availability availability() {
        return availability;
    }

    public Set<Skill> skills() {
        return Collections.unmodifiableSet(skills);
    }

    public List<PortfolioItem> portfolio() {
        return Collections.unmodifiableList(portfolio);
    }

    public BigDecimal ratingAverage() {
        return ratingAverage;
    }

    public int ratingCount() {
        return ratingCount;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    /**
     * Builder con validaciones centralizadas.
     */
    public static final class Builder {
        private final UUID userId;
        private UUID id = UUID.randomUUID();
        private String headline;
        private String bio;
        private HourlyRate hourlyRate;
        private Availability availability = Availability.AVAILABLE;
        private Set<Skill> skills = new LinkedHashSet<>();
        private List<PortfolioItem> portfolio = new java.util.ArrayList<>();
        private BigDecimal ratingAverage = BigDecimal.ZERO;
        private int ratingCount = 0;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        private Builder(UUID userId) {
            this.userId = Objects.requireNonNull(userId, "userId es obligatorio");
        }

        public Builder id(UUID value) {
            this.id = value;
            return this;
        }

        public Builder headline(String value) {
            this.headline = value;
            return this;
        }

        public Builder bio(String value) {
            this.bio = value;
            return this;
        }

        public Builder hourlyRate(HourlyRate value) {
            this.hourlyRate = value;
            return this;
        }

        public Builder availability(Availability value) {
            this.availability = value;
            return this;
        }

        public Builder skills(Set<Skill> value) {
            this.skills = value == null ? new LinkedHashSet<>() : new LinkedHashSet<>(value);
            return this;
        }

        public Builder portfolio(List<PortfolioItem> value) {
            this.portfolio = value == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(value);
            return this;
        }

        public Builder rating(BigDecimal average, int count) {
            this.ratingAverage = average == null ? BigDecimal.ZERO : average;
            this.ratingCount = count;
            return this;
        }

        public Builder createdAt(Instant value) {
            this.createdAt = value;
            return this;
        }

        public Builder updatedAt(Instant value) {
            this.updatedAt = value;
            return this;
        }

        public FreelancerProfile build() {
            if (headline == null || headline.isBlank()) {
                throw new ValidationException("El titular profesional es obligatorio");
            }
            if (availability == null) {
                throw new ValidationException("La disponibilidad es obligatoria");
            }
            return new FreelancerProfile(this);
        }
    }
}
