package com.freeconnect.projects.infrastructure.persistence;

import com.freeconnect.profiles.application.SkillCatalog;
import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectSearchCriteria;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression;
import com.freeconnect.shared.exception.ValidationException;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Traduce criterios y el AST del Interpreter a {@link Specification} JPA.
 *
 * <p><b>Patron: Interpreter (traduccion a SQL).</b> La misma expresion que se
 * evalua en memoria se convierte en predicados, manteniendo el filtrado en la
 * base de datos y el rendimiento paginado.</p>
 */
public final class ProjectSpecifications {

    private ProjectSpecifications() {
    }

    public static Specification<ProjectJpaEntity> from(ProjectSearchCriteria criteria, SkillCatalog skillCatalog) {
        List<Specification<ProjectJpaEntity>> specifications = new ArrayList<>();
        if (criteria.text() != null && !criteria.text().isBlank()) {
            specifications.add(textContains(criteria.text()));
        }
        if (criteria.category() != null) {
            specifications.add((root, query, cb) -> cb.equal(root.get("category"), criteria.category()));
        }
        if (criteria.modality() != null) {
            specifications.add((root, query, cb) -> cb.equal(root.get("modality"), criteria.modality()));
        }
        if (criteria.status() != null) {
            specifications.add((root, query, cb) -> cb.equal(root.get("status"), criteria.status()));
        }
        if (criteria.minBudget() != null) {
            specifications.add((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("budgetAmount"),
                    criteria.minBudget()));
        }
        if (criteria.maxBudget() != null) {
            specifications.add((root, query, cb) -> cb.lessThanOrEqualTo(root.get("budgetAmount"),
                    criteria.maxBudget()));
        }
        if (!criteria.skills().isEmpty()) {
            specifications.add(skillFilter(criteria.skills(), skillCatalog));
        }
        if (criteria.creatorId() != null) {
            specifications.add((root, query, cb) -> cb.equal(root.get("clientId"), criteria.creatorId()));
        }
        specifications.add(interpret(criteria.expression(), skillCatalog));
        return specifications.stream().reduce(Specification.where(null), Specification::and);
    }

    public static Specification<ProjectJpaEntity> interpret(ProjectFilterExpression expression,
                                                            SkillCatalog skillCatalog) {
        return (root, query, cb) -> toPredicate(expression, root, query, cb, skillCatalog);
    }

    private static Predicate toPredicate(ProjectFilterExpression expression, jakarta.persistence.criteria.Root<ProjectJpaEntity> root,
                                         jakarta.persistence.criteria.CriteriaQuery<?> query,
                                         jakarta.persistence.criteria.CriteriaBuilder cb, SkillCatalog skillCatalog) {
        if (expression instanceof ProjectFilterExpression.Always always) {
            return always.value() ? cb.conjunction() : cb.disjunction();
        }
        if (expression instanceof ProjectFilterExpression.And and) {
            return cb.and(toPredicate(and.left(), root, query, cb, skillCatalog),
                    toPredicate(and.right(), root, query, cb, skillCatalog));
        }
        if (expression instanceof ProjectFilterExpression.Or or) {
            return cb.or(toPredicate(or.left(), root, query, cb, skillCatalog),
                    toPredicate(or.right(), root, query, cb, skillCatalog));
        }
        ProjectFilterExpression.Comparison comparison = (ProjectFilterExpression.Comparison) expression;
        return comparisonPredicate(comparison, root, query, cb, skillCatalog);
    }

    private static Predicate comparisonPredicate(ProjectFilterExpression.Comparison comparison,
                                                 jakarta.persistence.criteria.Root<ProjectJpaEntity> root,
                                                 jakarta.persistence.criteria.CriteriaQuery<?> query,
                                                 jakarta.persistence.criteria.CriteriaBuilder cb,
                                                 SkillCatalog skillCatalog) {
        String field = comparison.field().toLowerCase(Locale.ROOT);
        boolean negated = comparison.operator() == ProjectFilterExpression.Operator.NE;
        return switch (field) {
            case "category" -> textPredicate(root.get("category"), comparison.value(), negated, cb,
                    ProjectCategory.class);
            case "modality" -> textPredicate(root.get("modality"), comparison.value(), negated, cb, Modality.class);
            case "status" -> textPredicate(root.get("status"), comparison.value(), negated, cb, ProjectStatus.class);
            case "budget" -> budgetPredicate(root.get("budgetAmount"), comparison, cb);
            case "skill" -> {
                UUID skillId = resolveSkill(comparison.value(), skillCatalog);
                if (skillId == null) {
                    yield cb.disjunction();
                }
                Join<ProjectJpaEntity, UUID> join = root.join("skillIds");
                if (query != null) {
                    query.distinct(true);
                }
                yield cb.equal(join, skillId);
            }
            case "title", "q" -> cb.like(cb.lower(root.get("title")),
                    "%" + comparison.value().toLowerCase(Locale.ROOT) + "%");
            case "description" -> cb.like(cb.lower(root.get("description")),
                    "%" + comparison.value().toLowerCase(Locale.ROOT) + "%");
            default -> throw new ValidationException("Campo de filtro no soportado: " + comparison.field());
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Predicate textPredicate(jakarta.persistence.criteria.Path path, String value, boolean negated,
                                           jakarta.persistence.criteria.CriteriaBuilder cb, Class<? extends Enum> type) {
        Object enumValue = Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
        return negated ? cb.notEqual(path, enumValue) : cb.equal(path, enumValue);
    }

    private static Predicate budgetPredicate(jakarta.persistence.criteria.Path<BigDecimal> path,
                                             ProjectFilterExpression.Comparison comparison,
                                             jakarta.persistence.criteria.CriteriaBuilder cb) {
        BigDecimal expected;
        try {
            expected = new BigDecimal(comparison.value());
        } catch (NumberFormatException ex) {
            throw new ValidationException("Valor de presupuesto invalido: " + comparison.value());
        }
        return switch (comparison.operator()) {
            case GT -> cb.greaterThan(path, expected);
            case GTE -> cb.greaterThanOrEqualTo(path, expected);
            case LT -> cb.lessThan(path, expected);
            case LTE -> cb.lessThanOrEqualTo(path, expected);
            case NE -> cb.notEqual(path, expected);
            default -> cb.equal(path, expected);
        };
    }

    private static UUID resolveSkill(String name, SkillCatalog catalog) {
        return catalog.all().stream()
                .filter(skill -> skill.name().equalsIgnoreCase(name))
                .map(Skill::id)
                .findFirst()
                .orElse(null);
    }

    private static Specification<ProjectJpaEntity> textContains(String text) {
        String pattern = "%" + text.toLowerCase(Locale.ROOT) + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern));
    }

    private static Specification<ProjectJpaEntity> skillFilter(java.util.Set<String> names, SkillCatalog catalog) {
        List<UUID> ids = names.stream()
                .map(name -> resolveSkill(name, catalog))
                .filter(java.util.Objects::nonNull)
                .toList();
        return (root, query, cb) -> {
            if (ids.isEmpty()) {
                return cb.disjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            return root.join("skillIds").in(ids);
        };
    }
}
