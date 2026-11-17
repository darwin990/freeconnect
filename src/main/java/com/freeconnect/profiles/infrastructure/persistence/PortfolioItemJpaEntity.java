package com.freeconnect.profiles.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Elemento de portafolio persistido (agregado independiente del perfil).
 */
@Entity
@Table(name = "portfolio_items")
public class PortfolioItemJpaEntity {

    @Id
    private UUID id;

    @Column(name = "profile_id", nullable = false)
    private UUID profileId;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(length = 1000)
    private String url;

    @Column(length = 1000)
    private String description;

    @Column(name = "storage_key", length = 500)
    private String storageKey;

    protected PortfolioItemJpaEntity() {
    }

    public PortfolioItemJpaEntity(UUID id, UUID profileId, String title, String url, String description) {
        this(id, profileId, title, url, description, null);
    }

    public PortfolioItemJpaEntity(UUID id, UUID profileId, String title, String url, String description,
                                  String storageKey) {
        this.id = id;
        this.profileId = profileId;
        this.title = title;
        this.url = url;
        this.description = description;
        this.storageKey = storageKey;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProfileId() {
        return profileId;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }
}
