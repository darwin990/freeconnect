package com.freeconnect.contracts.application;

import com.freeconnect.contracts.domain.commission.CommissionPlan;
import com.freeconnect.contracts.domain.commission.CommissionStrategy;
import com.freeconnect.contracts.domain.commission.PremiumCommissionStrategy;
import com.freeconnect.contracts.domain.commission.StandardCommissionStrategy;
import com.freeconnect.shared.exception.ValidationException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Selecciona la {@link CommissionStrategy} segun el plan solicitado.
 */
@Component
public class CommissionStrategyRegistry {

    private final Map<CommissionPlan, CommissionStrategy> strategies = new EnumMap<>(CommissionPlan.class);

    public CommissionStrategyRegistry(List<CommissionStrategy> available) {
        available.forEach(strategy -> strategies.put(strategy.plan(), strategy));
        strategies.putIfAbsent(CommissionPlan.STANDARD, new StandardCommissionStrategy());
        strategies.putIfAbsent(CommissionPlan.PREMIUM, new PremiumCommissionStrategy());
    }

    public CommissionStrategy of(CommissionPlan plan) {
        CommissionStrategy strategy = strategies.get(plan == null ? CommissionPlan.STANDARD : plan);
        if (strategy == null) {
            throw new ValidationException("Plan de comision no soportado: " + plan);
        }
        return strategy;
    }
}
