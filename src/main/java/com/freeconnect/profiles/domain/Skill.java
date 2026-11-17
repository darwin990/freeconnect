package com.freeconnect.profiles.domain;

import java.util.UUID;

/**
 * Habilidad inmutable compartida entre perfiles y proyectos.
 *
 * <p><b>Patron: Flyweight.</b> El objeto es inmutable y se comparte desde
 * {@code SkillCatalog}; el estado extrinseco (que perfil/proyecto la usa) vive
 * en las relaciones, no en la instancia.</p>
 */
public record Skill(UUID id, String name, String category) {

    public Skill {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la habilidad es obligatorio");
        }
        name = name.trim();
        category = category == null ? "GENERAL" : category.trim().toUpperCase();
    }
}
