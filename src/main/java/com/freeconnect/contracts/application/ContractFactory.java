package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.Milestone;
import com.freeconnect.contracts.domain.commission.CommissionPlan;
import com.freeconnect.contracts.domain.milestone.MilestoneFactory;
import com.freeconnect.contracts.domain.milestone.MilestoneSpec;
import com.freeconnect.contracts.domain.milestone.MilestoneType;
import com.freeconnect.shared.domain.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Fabrica de contratos.
 *
 * <p><b>Patron: Factory Method (variante creadora de agregados).</b> Genera el
 * identificador antes de construir los hitos, calcula la comision con la
 * estrategia seleccionada y deja el contrato listo para persistir.</p>
 */
@Component
public class ContractFactory {

    private final CommissionStrategyRegistry commissionStrategies;
    private final MilestoneFactory milestoneFactory = new MilestoneFactory();

    public ContractFactory(CommissionStrategyRegistry commissionStrategies) {
        this.commissionStrategies = commissionStrategies;
    }

    public Contract create(ContractDraft draft) {
        UUID contractId = UUID.randomUUID();
        Money commission = commissionStrategies.of(draft.commissionPlan()).commissionFor(draft.amount());
        List<Milestone> milestones = milestoneFactory.create(draft.milestoneType(), contractId, draft.title(),
                draft.amount(), draft.startDate(), draft.endDate(), draft.parts(), draft.specs());
        Contract contract = Contract.builder(draft.projectId(), draft.proposalId(), draft.clientId(),
                        draft.freelancerId())
                .id(contractId)
                .title(draft.title())
                .scope(draft.scope())
                .amount(draft.amount())
                .commission(commission)
                .startDate(draft.startDate())
                .endDate(draft.endDate())
                .milestones(milestones)
                .build();
        contract.markCreated(draft.clientId());
        return contract;
    }

    /**
     * Datos necesarios para construir un contrato.
     */
    public record ContractDraft(UUID projectId, UUID proposalId, UUID clientId, UUID freelancerId, String title,
                                String scope, Money amount, LocalDate startDate, LocalDate endDate,
                                CommissionPlan commissionPlan, MilestoneType milestoneType, int parts,
                                List<MilestoneSpec> specs) {
    }
}
