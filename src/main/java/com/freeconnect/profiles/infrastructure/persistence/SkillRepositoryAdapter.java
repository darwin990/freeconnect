package com.freeconnect.profiles.infrastructure.persistence;

import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.profiles.domain.SkillRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del catalogo de habilidades.
 */
@Repository
public class SkillRepositoryAdapter implements SkillRepository {

    private final SkillJpaRepository repository;

    public SkillRepositoryAdapter(SkillJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Skill> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Skill> findByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public Skill save(Skill skill) {
        SkillJpaEntity saved = repository.save(new SkillJpaEntity(skill.id(), skill.name(), skill.category()));
        return toDomain(saved);
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    private Skill toDomain(SkillJpaEntity entity) {
        return new Skill(entity.getId(), entity.getName(), entity.getCategory());
    }
}
