package com.freeconnect.projects.infrastructure.persistence;

import com.freeconnect.profiles.application.SkillCatalog;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectSearchCriteria;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.pagination.PagedResult;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * Adaptador JPA del repositorio de proyectos, con busqueda por Specification.
 */
@Repository
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final ProjectJpaRepository repository;
    private final SkillCatalog skillCatalog;

    public ProjectRepositoryAdapter(ProjectJpaRepository repository, SkillCatalog skillCatalog) {
        this.repository = repository;
        this.skillCatalog = skillCatalog;
    }

    @Override
    public Project save(Project project) {
        ProjectJpaEntity entity = repository.findById(project.id())
                .orElseGet(() -> new ProjectJpaEntity(project.id(), project.clientId(), project.title(),
                        project.description(), project.category(), project.modality(), project.status(),
                        project.budget().amount(), project.budget().currency(), project.deadline(),
                        project.publishedAt(), project.createdAt(), project.updatedAt()));
        entity.setTitle(project.title());
        entity.setDescription(project.description());
        entity.setCategory(project.category());
        entity.setModality(project.modality());
        entity.setStatus(project.status());
        entity.setBudgetAmount(project.budget().amount());
        entity.setBudgetCurrency(project.budget().currency());
        entity.setDeadline(project.deadline());
        entity.setPublishedAt(project.publishedAt());
        entity.setUpdatedAt(project.updatedAt());
        entity.replaceSkillIds(project.requiredSkills().stream().map(Skill::id)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        repository.save(entity);
        return project;
    }

    @Override
    public Optional<Project> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public PagedResult<Project> search(ProjectSearchCriteria criteria) {
        PageRequest pageRequest = PageRequest.of(criteria.page().page(), criteria.page().size(),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectJpaEntity> page = repository.findAll(
                ProjectSpecifications.from(criteria, skillCatalog), pageRequest);
        return PagedResult.of(page.getContent().stream().map(this::toDomain).toList(), page.getNumber(),
                page.getSize(), page.getTotalElements());
    }

    @Override
    public List<Project> findByClient(UUID clientId) {
        return repository.findByClientIdOrderByCreatedAtDesc(clientId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Project> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private Project toDomain(ProjectJpaEntity entity) {
        Set<Skill> skills = entity.getSkillIds().stream()
                .map(skillCatalog::find)
                .flatMap(Optional::stream)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return Project.builder(entity.getClientId())
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .category(entity.getCategory())
                .budget(Money.of(entity.getBudgetAmount(), entity.getBudgetCurrency()))
                .modality(entity.getModality())
                .requiredSkills(skills)
                .deadline(entity.getDeadline())
                .status(entity.getStatus())
                .publishedAt(entity.getPublishedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
