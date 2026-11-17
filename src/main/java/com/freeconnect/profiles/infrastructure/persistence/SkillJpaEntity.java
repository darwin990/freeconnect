package com.freeconnect.profiles.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * Catalogo de habilidades persistido (fuente del Flyweight SkillCatalog).
 */
@Entity
@Table(name = "skills")
public class SkillJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(nullable = false, length = 60)
    private String category;

    protected SkillJpaEntity() {
    }

    public SkillJpaEntity(UUID id, String name, String category) {
        this.id = id;
        this.name = name;
        this.category = category;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }
}
