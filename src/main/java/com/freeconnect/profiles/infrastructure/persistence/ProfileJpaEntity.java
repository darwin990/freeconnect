package com.freeconnect.profiles.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Modelo JPA unico para perfiles de freelancer y cliente.
 */
@Entity
@Table(name = "profiles")
public class ProfileJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(length = 160)
    private String headline;

    @Column(length = 2000)
    private String bio;

    @Column(name = "hourly_rate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(length = 3)
    private String currency;

    @Column(length = 20)
    private String availability;

    @Column(name = "company_name", length = 160)
    private String companyName;

    @Column(length = 2000)
    private String description;

    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage;

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill_id", nullable = false)
    private Set<UUID> skillIds = new LinkedHashSet<>();

    protected ProfileJpaEntity() {
    }

    public ProfileJpaEntity(UUID id, UUID userId, String type, String headline, String bio, BigDecimal hourlyRate,
                            String currency, String availability, String companyName, String description,
                            BigDecimal ratingAverage, int ratingCount, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.headline = headline;
        this.bio = bio;
        this.hourlyRate = hourlyRate;
        this.currency = currency;
        this.availability = availability;
        this.companyName = companyName;
        this.description = description;
        this.ratingAverage = ratingAverage;
        this.ratingCount = ratingCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getType() {
        return type;
    }

    public String getHeadline() {
        return headline;
    }

    public String getBio() {
        return bio;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAvailability() {
        return availability;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<UUID> getSkillIds() {
        return skillIds;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRatingAverage(BigDecimal ratingAverage) {
        this.ratingAverage = ratingAverage;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void replaceSkillIds(Set<UUID> ids) {
        this.skillIds.clear();
        this.skillIds.addAll(ids);
    }
}
