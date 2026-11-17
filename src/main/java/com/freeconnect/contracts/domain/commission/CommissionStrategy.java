package com.freeconnect.contracts.domain.commission;

import com.freeconnect.shared.domain.Money;

/**
 * Estrategia de calculo de comision.
 *
 * <p><b>Patron: Strategy.</b> El contrato no conoce el porcentaje; delega en la
 * estrategia seleccionada por plan, lo que permite agregar planes sin tocar el
 * agregado.</p>
 */
public interface CommissionStrategy {

    CommissionPlan plan();

    Money commissionFor(Money amount);
}
