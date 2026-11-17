package com.freeconnect.projects.domain.filter;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.util.Locale;

/**
 * AST del lenguaje de filtros de proyectos.
 *
 * <p><b>Patron: Interpreter.</b> Cada nodo interpreta una expresion sobre un
 * {@link Project}; el parser construye el arbol y la infraestructura lo traduce
 * tambien a SQL para no cargar todo en memoria.</p>
 */
public sealed interface ProjectFilterExpression
        permits ProjectFilterExpression.And, ProjectFilterExpression.Or,
        ProjectFilterExpression.Comparison, ProjectFilterExpression.Always {

    boolean matches(Project project);

    record And(ProjectFilterExpression left, ProjectFilterExpression right) implements ProjectFilterExpression {
        @Override
        public boolean matches(Project project) {
            return left.matches(project) && right.matches(project);
        }
    }

    record Or(ProjectFilterExpression left, ProjectFilterExpression right) implements ProjectFilterExpression {
        @Override
        public boolean matches(Project project) {
            return left.matches(project) || right.matches(project);
        }
    }

    record Always(boolean value) implements ProjectFilterExpression {
        @Override
        public boolean matches(Project project) {
            return value;
        }
    }

    record Comparison(String field, Operator operator, String value) implements ProjectFilterExpression {

        @Override
        public boolean matches(Project project) {
            String normalizedField = field.toLowerCase(Locale.ROOT);
            return switch (normalizedField) {
                case "category" -> compareText(project.category().name());
                case "modality" -> compareText(project.modality().name());
                case "status" -> compareText(project.status().name());
                case "budget" -> compareNumber(project.budget().amount());
                case "skill" -> project.requiredSkills().stream()
                        .anyMatch(skill -> skill.name().equalsIgnoreCase(value));
                case "title", "q" -> project.title().toLowerCase(Locale.ROOT)
                        .contains(value.toLowerCase(Locale.ROOT));
                case "description" -> project.description() != null
                        && project.description().toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT));
                default -> throw new ValidationException("Campo de filtro no soportado: " + field);
            };
        }

        private boolean compareText(String actual) {
            boolean equals = actual.equalsIgnoreCase(value);
            return operator == Operator.NE ? !equals : equals;
        }

        private boolean compareNumber(BigDecimal actual) {
            BigDecimal expected;
            try {
                expected = new BigDecimal(value);
            } catch (NumberFormatException ex) {
                throw new ValidationException("Valor numerico invalido: " + value);
            }
            int comparison = actual.compareTo(expected);
            return switch (operator) {
                case GT -> comparison > 0;
                case GTE -> comparison >= 0;
                case LT -> comparison < 0;
                case LTE -> comparison <= 0;
                case NE -> comparison != 0;
                default -> comparison == 0;
            };
        }
    }

    enum Operator {
        EQ,
        NE,
        GT,
        GTE,
        LT,
        LTE,
        CONTAINS
    }
}
