package com.freeconnect.contracts.domain.commission;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.freeconnect.shared.domain.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las estrategias de comision (patron Strategy).
 */
class CommissionStrategyTest {

    private final Money amount = Money.of(new BigDecimal("1000.00"), "USD");

    @Test
    void standardPlanChargesTenPercent() {
        CommissionStrategy strategy = new StandardCommissionStrategy();

        assertEquals(CommissionPlan.STANDARD, strategy.plan());
        assertEquals(new BigDecimal("100.00"), strategy.commissionFor(amount).amount());
    }

    @Test
    void premiumPlanChargesFivePercent() {
        CommissionStrategy strategy = new PremiumCommissionStrategy();

        assertEquals(CommissionPlan.PREMIUM, strategy.plan());
        assertEquals(new BigDecimal("50.00"), strategy.commissionFor(amount).amount());
    }

    @Test
    void strategiesAreInterchangeableWithoutChangingTheAggregate() {
        Money standard = new StandardCommissionStrategy().commissionFor(amount);
        Money premium = new PremiumCommissionStrategy().commissionFor(amount);

        assertTrue(standard.isGreaterThan(premium));
        assertEquals("USD", premium.currency());
    }
}
