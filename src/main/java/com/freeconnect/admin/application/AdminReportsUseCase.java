package com.freeconnect.admin.application;

import com.freeconnect.admin.domain.report.AdminReport;
import com.freeconnect.admin.domain.report.ContractReport;
import com.freeconnect.admin.domain.report.ProjectReport;
import com.freeconnect.admin.domain.report.ReportElement;
import com.freeconnect.admin.domain.report.UserReport;
import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.contracts.domain.ContractStatus;
import com.freeconnect.identity.domain.UserRepository;
import com.freeconnect.identity.domain.UserStatus;
import com.freeconnect.projects.domain.Project;
import com.freeconnect.projects.domain.ProjectRepository;
import com.freeconnect.projects.domain.ProjectStatus;
import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.security.CurrentUserProvider;
import com.freeconnect.shared.security.Role;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso de reportes administrativos (Visitor).
 */
@Service
public class AdminReportsUseCase implements UseCase<Void, List<AdminReport>> {

    private final ProjectRepository projectRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final CurrentUserProvider currentUser;
    private final Clock clock;

    public AdminReportsUseCase(ProjectRepository projectRepository, ContractRepository contractRepository,
                               UserRepository userRepository, CurrentUserProvider currentUser, Clock clock) {
        this.projectRepository = projectRepository;
        this.contractRepository = contractRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminReport> execute(Void ignored) {
        currentUser.requireAdmin();
        List<Project> projects = projectRepository.findAll();
        List<Contract> contracts = contractRepository.findAll();

        ProjectReport projectReport = new ProjectReport(projects.size(),
                count(projects, project -> project.status() == ProjectStatus.PUBLISHED),
                count(projects, project -> project.status() == ProjectStatus.IN_PROGRESS),
                count(projects, project -> project.status() == ProjectStatus.FINISHED),
                count(projects, project -> project.status() == ProjectStatus.CANCELLED),
                sums(projects, project -> project.budget()));

        ContractReport contractReport = new ContractReport(contracts.size(),
                count(contracts, contract -> contract.status() == ContractStatus.ACTIVE),
                count(contracts, contract -> contract.status() == ContractStatus.COMPLETED),
                count(contracts, contract -> contract.status() == ContractStatus.CANCELLED),
                sums(contracts, Contract::amount),
                sums(contracts, Contract::commission));

        UserReport userReport = new UserReport(userRepository.count(),
                userRepository.countByRole(Role.CLIENT),
                userRepository.countByRole(Role.FREELANCER),
                userRepository.countByRole(Role.ADMIN),
                userRepository.countByStatus(UserStatus.SUSPENDED));

        StatisticsReportVisitor visitor = new StatisticsReportVisitor(clock);
        List<ReportElement> elements = List.of(projectReport, contractReport, userReport);
        return elements.stream().map(element -> element.accept(visitor)).toList();
    }

    private <T> int count(List<T> items, java.util.function.Predicate<T> predicate) {
        return (int) items.stream().filter(predicate).count();
    }

    private <T> Map<String, BigDecimal> sums(List<T> items, Function<T, Money> extractor) {
        return items.stream()
                .map(extractor)
                .collect(Collectors.groupingBy(Money::currency, LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, Money::amount, BigDecimal::add)));
    }
}
