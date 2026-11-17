package com.freeconnect.contracts.api.dto;

import com.freeconnect.contracts.domain.commission.CommissionPlan;
import com.freeconnect.contracts.domain.milestone.MilestoneType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Payload opcional para aceptar una propuesta y definir el contrato.
 */
public record AcceptProposalRequest(
        @Size(max = 180) String title,
        @Size(max = 4000) String scope,
        LocalDate startDate,
        LocalDate endDate,
        CommissionPlan commissionPlan,
        MilestoneType milestoneType,
        Integer parts,
        List<MilestoneRequest> milestones) {

    /**
     * Hito explicito definido por el cliente.
     */
    public record MilestoneRequest(
            @Size(max = 180) String title,
            @Size(max = 4000) String description,
            @DecimalMin(value = "0.01") BigDecimal amount,
            LocalDate dueDate) {
    }
}
