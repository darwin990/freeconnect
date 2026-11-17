package com.freeconnect.projects.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freeconnect.profiles.domain.Skill;
import com.freeconnect.projects.domain.filter.ProjectFilterExpression;
import com.freeconnect.projects.domain.filter.ProjectFilterParser;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del lenguaje de filtros (patron Interpreter).
 */
class ProjectFilterInterpreterTest {

    private final Project project = Project.builder(UUID.randomUUID())
            .title("API de reservas")
            .description("Construir una API REST con Spring Boot y PostgreSQL")
            .category(ProjectCategory.SOFTWARE)
            .budget(Money.of(new BigDecimal("1500.00"), "USD"))
            .modality(Modality.REMOTE)
            .requiredSkills(Set.of(new Skill(UUID.randomUUID(), "Java", "BACKEND")))
            .status(ProjectStatus.PUBLISHED)
            .build();

    @Test
    void blankExpressionMatchesEverything() {
        assertTrue(ProjectFilterParser.parse("  ").matches(project));
    }

    @Test
    void interpretsComparisonAndBooleanOperators() {
        ProjectFilterExpression expression = ProjectFilterParser.parse(
                "budget >= 1000 AND modality = REMOTE AND status = PUBLISHED");

        assertTrue(expression.matches(project));
    }

    @Test
    void interpretsTextAndSkillFields() {
        assertTrue(ProjectFilterParser.parse("title contains \"reservas\"").matches(project));
        assertTrue(ProjectFilterParser.parse("skill = java").matches(project));
        assertFalse(ProjectFilterParser.parse("skill = Kotlin").matches(project));
    }

    @Test
    void honoursParenthesesAndOrPrecedence() {
        ProjectFilterExpression expression = ProjectFilterParser.parse(
                "(category = DESIGN OR category = SOFTWARE) AND budget >= 1000");

        assertTrue(expression.matches(project));
        assertFalse(ProjectFilterParser.parse("category = DESIGN OR category = SOFTWARE AND budget > 5000")
                .matches(project));
    }

    @Test
    void rejectsUnknownFieldsAndInvalidOperators() {
        assertThrows(ValidationException.class, () -> ProjectFilterParser.parse("unknown = value"));
        assertThrows(ValidationException.class, () -> ProjectFilterParser.parse("budget contains 100"));
        assertThrows(ValidationException.class, () -> ProjectFilterParser.parse("(category = SOFTWARE"));
    }
}
