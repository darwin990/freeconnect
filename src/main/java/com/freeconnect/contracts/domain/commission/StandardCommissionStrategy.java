package com.freeconnect.contracts.domain.commission;

import com.freeconnect.shared.domain.Money;
import java.math.BigDecimal;

/**
 * Comision estandar de la plataforma: 10%.
 */
public class StandardCommissionStrategy implements CommissionStrategy {

    private static final BigDecimal RATE = new BigDecimal("0.10");

    @Override
    public CommissionPlan plan() {
        return CommissionPlan.STANDARD;
    }

    @Override
    public Money commissionFor(Money amount) {
        return amount.percentage(RATE);
    }
}
