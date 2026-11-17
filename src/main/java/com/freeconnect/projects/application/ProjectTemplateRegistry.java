package com.freeconnect.projects.application;

import com.freeconnect.profiles.application.SkillCatalog;
import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectTemplate;
import com.freeconnect.shared.domain.Money;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Registro de plantillas de proyecto disponibles.
 *
 * <p><b>Patron: Prototype (registry).</b> Entrega copias clonadas para que cada
 * cliente personalice su proyecto sin mutar la plantilla compartida.</p>
 */
@Component
public class ProjectTemplateRegistry {

    private final Map<UUID, ProjectTemplate> templates = new LinkedHashMap<>();

    public ProjectTemplateRegistry(SkillCatalog skills) {
        register(new ProjectTemplate(stableId("web-app"), "Aplicacion web",
                "Desarrollo de aplicacion web con backend y frontend",
                "Construccion de una aplicacion web responsive con autenticacion, API REST y base de datos.",
                ProjectCategory.SOFTWARE, Money.of(new BigDecimal("3500"), "USD"), Modality.REMOTE,
                Set.of(skills.resolve("Java"), skills.resolve("Spring Boot"), skills.resolve("React")), 45));
        register(new ProjectTemplate(stableId("mobile-app"), "Aplicacion movil",
                "Aplicacion movil multiplataforma",
                "Desarrollo de app movil con Flutter, integracion con API y publicacion en tiendas.",
                ProjectCategory.SOFTWARE, Money.of(new BigDecimal("4200"), "USD"), Modality.HYBRID,
                Set.of(skills.resolve("Flutter"), skills.resolve("Dart")), 60));
        register(new ProjectTemplate(stableId("branding"), "Identidad de marca",
                "Diseno de identidad visual",
                "Investigacion, logotipo, paleta, tipografia y manual de marca.",
                ProjectCategory.DESIGN, Money.of(new BigDecimal("1200"), "USD"), Modality.REMOTE,
                Set.of(skills.resolve("Branding"), skills.resolve("Illustrator")), 30));
        register(new ProjectTemplate(stableId("data-pipeline"), "Pipeline de datos",
                "Construccion de pipeline analitico",
                "Ingesta, transformacion y visualizacion de datos con calidad y monitoreo.",
                ProjectCategory.DATA, Money.of(new BigDecimal("5000"), "USD"), Modality.REMOTE,
                Set.of(skills.resolve("Python"), skills.resolve("SQL"), skills.resolve("Airflow")), 90));
    }

    private void register(ProjectTemplate template) {
        templates.put(template.id(), template);
    }

    public List<ProjectTemplate> list() {
        return List.copyOf(templates.values());
    }

    public Optional<ProjectTemplate> find(UUID id) {
        return Optional.ofNullable(templates.get(id));
    }

    /** Devuelve una copia (Prototype) de la plantilla solicitada. */
    public Optional<ProjectTemplate> copy(UUID id) {
        return find(id).map(ProjectTemplate::copy);
    }

    private static UUID stableId(String name) {
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }
}
