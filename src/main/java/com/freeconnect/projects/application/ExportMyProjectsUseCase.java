package com.freeconnect.projects.application;

import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectSearchCriteria;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.pagination.PageLoader;
import com.freeconnect.shared.pagination.PageQuery;
import com.freeconnect.shared.pagination.PagedIterator;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: exportar en CSV todos los proyectos del cliente autenticado.
 *
 * <p><b>Patron: Iterator.</b> {@link PagedIterator} recorre las paginas del
 * repositorio sin cargar el historico completo en memoria.</p>
 */
@Service
public class ExportMyProjectsUseCase implements UseCase<Void, ExportMyProjectsUseCase.CsvExport> {

    private static final int PAGE_SIZE = 50;

    private final ProjectRepository projectRepository;
    private final CurrentUserProvider currentUser;

    public ExportMyProjectsUseCase(ProjectRepository projectRepository, CurrentUserProvider currentUser) {
        this.projectRepository = projectRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public CsvExport execute(Void input) {
        currentUser.requireRole(Role.CLIENT);
        ProjectSearchCriteria criteria = new ProjectSearchCriteria(null, null, null, null, null, null,
                currentUser.requireUserId(), java.util.Set.of(), null, PageQuery.of(0, PAGE_SIZE));
        PageLoader<Project> loader = (page, size) -> projectRepository.search(criteria.withPage(PageQuery.of(page, size)));
        PagedIterator<Project> iterator = new PagedIterator<>(loader, PAGE_SIZE);
        StringBuilder csv = new StringBuilder("id,title,status,category,modality,budget,currency,createdAt\n");
        long count = 0;
        while (iterator.hasNext()) {
            Project project = iterator.next();
            csv.append(escape(project.id().toString())).append(',')
                    .append(escape(project.title())).append(',')
                    .append(project.status()).append(',')
                    .append(project.category()).append(',')
                    .append(project.modality()).append(',')
                    .append(project.budget().amount()).append(',')
                    .append(project.budget().currency()).append(',')
                    .append(project.createdAt()).append('\n');
            count++;
        }
        return new CsvExport(csv.toString(), count);
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    public record CsvExport(String content, long rows) {
    }
}
