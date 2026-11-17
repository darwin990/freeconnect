package com.freeconnect.profiles.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia del catalogo de habilidades.
 */
public interface SkillRepository {

    List<Skill> findAll();

    Optional<Skill> findByNameIgnoreCase(String name);

    Skill save(Skill skill);

    Optional<Skill> findById(UUID id);
}
