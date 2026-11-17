package com.freeconnect.contracts.domain.commission;

import com.freeconnect.shared.domain.Money;
import java.math.BigDecimal;

/**
 * Comision reducida para clientes premium: 5%.
 */
public class PremiumCommissionStrategy implements CommissionStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.05");

    @Override
    public CommissionPlan plan() {
        return CommissionPlan.PREMIUM;
    }

    @Override
    public Money commissionFor(Money amount) {
        return amount.percentage(RATE);
    }
}
