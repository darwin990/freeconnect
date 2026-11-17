package com.freeconnect.profiles.domain;

import com.freeconnect.shared.exception.ValidationException;
import java.time.Instant;
import java.util.UUID;

/**
 * Perfil de cliente (empresa o particular).
 */
public class ClientProfile {

    private final UUID id;
    private final UUID userId;
    private String companyName;
    private String description;
    private final Instant createdAt;
    private Instant updatedAt;

    private ClientProfile(UUID id, UUID userId, String companyName, String description, Instant createdAt,
                          Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.companyName = companyName;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ClientProfile create(UUID userId, String companyName, String description) {
        validate(companyName);
        Instant now = Instant.now();
        return new ClientProfile(UUID.randomUUID(), userId, companyName.trim(), description, now, now);
    }

    public static ClientProfile rehydrate(UUID id, UUID userId, String companyName, String description,
                                          Instant createdAt, Instant updatedAt) {
        return new ClientProfile(id, userId, companyName, description, createdAt, updatedAt);
    }

    public void update(String companyName, String description) {
        validate(companyName);
        this.companyName = companyName.trim();
        this.description = description;
        this.updatedAt = Instant.now();
    }

    private static void validate(String companyName) {
        if (companyName == null || companyName.isBlank()) {
            throw new ValidationException("El nombre de la empresa es obligatorio");
        }
    }

    public UUID id() {
        return id;
    }

    public UUID userId() {
        return userId;
    }

    public String companyName() {
        return companyName;
    }

    public String description() {
        return description;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}
