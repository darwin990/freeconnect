package com.freeconnect.contracts.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freeconnect.contracts.domain.command.CompleteMilestoneCommand;
import com.freeconnect.contracts.domain.command.ContractCommandInvoker;
import com.freeconnect.contracts.domain.commission.StandardCommissionStrategy;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.DomainException;
import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del agregado de contrato: Builder, State y Command (undo).
 */
class ContractDomainTest {

    private static final UUID CLIENT = UUID.randomUUID();
    private static final UUID FREELANCER = UUID.randomUUID();

    @Test
    void builderRejectsContractWithoutMilestones() {
        assertThrows(ValidationException.class, () -> Contract.builder(UUID.randomUUID(), UUID.randomUUID(),
                        CLIENT, FREELANCER)
                .title("Contrato")
                .scope("Alcance")
                .amount(money("1000"))
                .commission(money("100"))
                .build());
    }

    @Test
    void builderRejectsMilestonesExceedingContractAmount() {
        UUID contractId = UUID.randomUUID();
        Milestone expensive = milestone(contractId, "Hito", "1700", MilestoneStatus.PENDING);
        assertThrows(ValidationException.class, () -> Contract.builder(UUID.randomUUID(), UUID.randomUUID(),
                        CLIENT, FREELANCER)
                .id(contractId)
                .title("Contrato")
                .scope("Alcance")
                .amount(money("1000"))
                .commission(money("100"))
                .milestones(List.of(expensive))
                .build());
    }

    @Test
    void draftContractCannotBeCompletedDirectly() {
        Contract contract = activeContract();
        contract.transitionTo(ContractStatus.CANCELLED, CLIENT);
        assertThrows(DomainException.class, () -> contract.transitionTo(ContractStatus.ACTIVE, CLIENT));
    }

    @Test
    void contractCompletesOnlyWhenEveryMilestoneIsApproved() {
        Contract contract = activeContract();

        contract.approveMilestone(contract.milestones().get(0).id(), CLIENT);
        assertEquals(ContractStatus.ACTIVE, contract.status());
        assertEquals(82, contract.progress());

        contract.approveMilestone(contract.milestones().get(1).id(), CLIENT);
        assertEquals(ContractStatus.COMPLETED, contract.status());
        assertEquals(100, contract.progress());
        assertEquals(2, contract.approvedMilestones());
        assertTrue(contract.pullDomainEvents().stream().anyMatch(e -> e instanceof ContractCompletedEvent));
    }

    @Test
    void commandCanUndoMilestoneApprovalAndContractCompletion() {
        Contract contract = activeContract();
        UUID firstMilestone = contract.milestones().get(0).id();
        ContractCommandInvoker invoker = new ContractCommandInvoker();

        invoker.run(new CompleteMilestoneCommand(contract, firstMilestone, CLIENT));
        assertEquals(MilestoneStatus.APPROVED, contract.milestone(firstMilestone).status());
        assertEquals(ContractStatus.ACTIVE, contract.status());

        assertTrue(invoker.undoLast());
        assertEquals(MilestoneStatus.SUBMITTED, contract.milestone(firstMilestone).status());
        assertEquals(ContractStatus.ACTIVE, contract.status());
        assertTrue(invoker.history().isEmpty());
    }

    private Contract activeContract() {
        UUID contractId = UUID.randomUUID();
        Milestone first = milestone(contractId, "Hito 1", "500", MilestoneStatus.SUBMITTED);
        Milestone second = milestone(contractId, "Hito 2", "500", MilestoneStatus.SUBMITTED);
        return Contract.builder(UUID.randomUUID(), UUID.randomUUID(), CLIENT, FREELANCER)
                .id(contractId)
                .title("Contrato de prueba")
                .scope("Construir la API")
                .amount(money("1000"))
                .commission(new StandardCommissionStrategy().commissionFor(money("1000")))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .milestones(List.of(first, second))
                .status(ContractStatus.ACTIVE)
                .build();
    }

    private Milestone milestone(UUID contractId, String title, String amount, MilestoneStatus status) {
        return Milestone.rehydrate(UUID.randomUUID(), contractId, title, "Descripcion", money(amount),
                LocalDate.now().plusDays(15), 0, status,
                List.of(Deliverable.submit("Entregable " + title, "Detalle", "https://example.com")));
    }

    private Money money(String value) {
        return Money.of(new BigDecimal(value), "USD");
    }
}
