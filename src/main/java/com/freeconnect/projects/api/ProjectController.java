package com.freeconnect.projects.api;

import com.freeconnect.projects.api.dto.ChangeProjectStatusRequest;
import com.freeconnect.projects.api.dto.ProjectRequest;
import com.freeconnect.projects.api.dto.ProjectTemplateView;
import com.freeconnect.projects.application.ChangeProjectStatusUseCase;
import com.freeconnect.projects.application.CreateProjectCommand;
import com.freeconnect.projects.application.CreateProjectFromTemplateUseCase;
import com.freeconnect.projects.application.CreateProjectUseCase;
import com.freeconnect.projects.application.ExportMyProjectsUseCase;
import com.freeconnect.projects.application.GetProjectUseCase;
import com.freeconnect.projects.application.ProjectTemplateRegistry;
import com.freeconnect.projects.application.ProjectView;
import com.freeconnect.projects.application.SearchProjectsQuery;
import com.freeconnect.projects.application.SearchProjectsUseCase;
import com.freeconnect.projects.application.UpdateProjectUseCase;
import com.freeconnect.projects.domain.Modality;
import com.freeconnect.projects.domain.ProjectCategory;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.shared.api.PageResponse;
import com.freeconnect.shared.pagination.PageQuery;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de proyectos (RF-03): publicacion, edicion, busqueda y filtros.
 */
@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Proyectos", description = "Ciclo de vida y busqueda de proyectos")
public class ProjectController {

    private final CreateProjectUseCase createProjectUseCase;
    private final UpdateProjectUseCase updateProjectUseCase;
    private final ChangeProjectStatusUseCase changeProjectStatusUseCase;
    private final GetProjectUseCase getProjectUseCase;
    private final SearchProjectsUseCase searchProjectsUseCase;
    private final CreateProjectFromTemplateUseCase createProjectFromTemplateUseCase;
    private final ExportMyProjectsUseCase exportMyProjectsUseCase;
    private final ProjectTemplateRegistry templateRegistry;

    public ProjectController(CreateProjectUseCase createProjectUseCase, UpdateProjectUseCase updateProjectUseCase,
                            ChangeProjectStatusUseCase changeProjectStatusUseCase, GetProjectUseCase getProjectUseCase,
                            SearchProjectsUseCase searchProjectsUseCase,
                            CreateProjectFromTemplateUseCase createProjectFromTemplateUseCase,
                            ExportMyProjectsUseCase exportMyProjectsUseCase,
                            ProjectTemplateRegistry templateRegistry) {
        this.createProjectUseCase = createProjectUseCase;
        this.updateProjectUseCase = updateProjectUseCase;
        this.changeProjectStatusUseCase = changeProjectStatusUseCase;
        this.getProjectUseCase = getProjectUseCase;
        this.searchProjectsUseCase = searchProjectsUseCase;
        this.createProjectFromTemplateUseCase = createProjectFromTemplateUseCase;
        this.exportMyProjectsUseCase = exportMyProjectsUseCase;
        this.templateRegistry = templateRegistry;
    }

    @GetMapping
    @Operation(summary = "Buscar proyectos paginados por texto, categoria, presupuesto, modalidad y filtro DSL")
    public PageResponse<ProjectView> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) ProjectCategory category,
            @RequestParam(required = false) Modality modality,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) BigDecimal minBudget,
            @RequestParam(required = false) BigDecimal maxBudget,
            @RequestParam(required = false) List<String> skill,
            @RequestParam(required = false) String filter) {
        Set<String> skills = skill == null ? Set.of() : skill.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
        SearchProjectsQuery query = new SearchProjectsQuery(q, category, modality, status, minBudget, maxBudget,
                skills, filter, PageQuery.of(page, size), null);
        return PageResponse.from(searchProjectsUseCase.execute(query));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalle de un proyecto")
    public ProjectView get(@PathVariable UUID id) {
        return getProjectUseCase.execute(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Crear un proyecto en estado DRAFT")
    public ResponseEntity<ProjectView> create(@Valid @RequestBody ProjectRequest request) {
        ProjectView created = createProjectUseCase.execute(new CreateProjectCommand(request.title(),
                request.description(), request.category(), request.budget(), request.currency(), request.modality(),
                request.deadline(), request.skills()));
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Editar un proyecto propio")
    public ProjectView update(@PathVariable UUID id, @Valid @RequestBody ProjectRequest request) {
        return updateProjectUseCase.execute(new UpdateProjectUseCase.UpdateProjectCommand(id, request.title(),
                request.description(), request.category(), request.budget(), request.currency(), request.modality(),
                request.deadline(), request.skills()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Cambiar estado (State pattern) con auditoria y eventos")
    public ProjectView changeStatus(@PathVariable UUID id, @Valid @RequestBody ChangeProjectStatusRequest request) {
        return changeProjectStatusUseCase.execute(
                new ChangeProjectStatusUseCase.Command(id, request.status()));
    }

    @GetMapping("/templates")
    @Operation(summary = "Catalogo de plantillas clonables (Prototype)")
    public List<ProjectTemplateView> templates() {
        return templateRegistry.list().stream().map(ProjectTemplateView::from).toList();
    }

    @PostMapping("/from-template/{templateId}")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Crear proyecto clonando una plantilla")
    public ResponseEntity<ProjectView> fromTemplate(@PathVariable UUID templateId) {
        ProjectView created = createProjectFromTemplateUseCase.execute(templateId);
        return ResponseEntity.created(URI.create("/api/v1/projects/" + created.id())).body(created);
    }

    @GetMapping(value = "/mine/export", produces = "text/csv")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Exportar mis proyectos (Iterator paginado)")
    public ResponseEntity<String> exportMine() {
        ExportMyProjectsUseCase.CsvExport export = exportMyProjectsUseCase.execute(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"proyectos.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(export.content());
    }
}
