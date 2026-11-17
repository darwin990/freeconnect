package com.freeconnect.profiles.application;

import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.profiles.domain.SkillRepository;
import jakarta.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Fabrica/cache de instancias inmutables de {@link Skill}.
 *
 * <p><b>Patron: Flyweight.</b> Evita duplicar objetos de habilidad en memoria:
 * todos los perfiles y proyectos comparten la misma instancia canonica indexada
 * por nombre normalizado; el estado extrinseco se guarda en las tablas puente.</p>
 */
@Service
public class SkillCatalog {

    private final SkillRepository repository;
    private final Map<String, Skill> cache = new ConcurrentHashMap<>();

    public SkillCatalog(SkillRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    void warmUp() {
        repository.findAll().forEach(skill -> cache.put(key(skill.name()), skill));
    }

    /** Resuelve (o crea) una habilidad compartida. */
    public Skill resolve(String name) {
        return resolve(name, "GENERAL");
    }

    public Skill resolve(String name, String category) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("La habilidad no puede estar vacia");
        }
        String normalized = key(name);
        return cache.computeIfAbsent(normalized, ignored -> repository.findByNameIgnoreCase(name.trim())
                .orElseGet(() -> repository.save(new Skill(UUID.randomUUID(), name.trim(), category))));
    }

    public Optional<Skill> find(UUID id) {
        return cache.values().stream().filter(skill -> skill.id().equals(id)).findFirst()
                .or(() -> repository.findById(id));
    }

    public Collection<Skill> all() {
        return List.copyOf(cache.values());
    }

    private String key(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
