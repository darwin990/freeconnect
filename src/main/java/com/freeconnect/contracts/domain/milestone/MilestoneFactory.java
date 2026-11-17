package com.freeconnect.contracts.domain.milestone;

import com.freeconnect.contracts.domain.Milestone;
import com.freeconnect.shared.domain.Money;
import com.freeconnect.shared.exception.ValidationException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Fabrica de hitos.
 *
 * <p><b>Patron: Factory Method.</b> Centraliza la creacion de las distintas
 * variantes de hito (unico, repartido o definido por el cliente) para que el
 * agregado {@code Contract} no dependa de los detalles de construccion.</p>
 */
public final class MilestoneFactory {

    public List<Milestone> create(MilestoneType type, UUID contractId, String baseTitle, Money total,
                                  LocalDate startDate, LocalDate endDate, int parts,
                                  List<MilestoneSpec> specs) {
        return switch (type == null ? MilestoneType.SINGLE : type) {
            case SINGLE -> List.of(Milestone.create(contractId, baseTitle, "Pago unico del contrato", total,
                    endDate, 0));
            case EQUAL_SPLIT -> split(contractId, baseTitle, total, startDate, endDate, parts);
            case CUSTOM -> custom(contractId, specs, total);
        };
    }

    private List<Milestone> split(UUID contractId, String baseTitle, Money total, LocalDate startDate,
                                  LocalDate endDate, int parts) {
        if (parts < 1 || parts > 12) {
            throw new ValidationException("La cantidad de hitos debe estar entre 1 y 12");
        }
        BigDecimal perPart = total.amount().divide(BigDecimal.valueOf(parts), 2, RoundingMode.DOWN);
        BigDecimal assigned = BigDecimal.ZERO;
        List<Milestone> milestones = new ArrayList<>();
        for (int index = 0; index < parts; index++) {
            BigDecimal value = index == parts - 1 ? total.amount().subtract(assigned) : perPart;
            assigned = assigned.add(value);
            LocalDate due = dueDate(startDate, endDate, index + 1, parts);
            milestones.add(Milestone.create(contractId, "%s - Hito %d/%d".formatted(baseTitle, index + 1, parts),
                    "Pago parcial %d de %d".formatted(index + 1, parts), Money.of(value, total.currency()), due,
                    index));
        }
        return milestones;
    }

    private List<Milestone> custom(UUID contractId, List<MilestoneSpec> specs, Money total) {
        if (specs == null || specs.isEmpty()) {
            throw new ValidationException("Debe definir al menos un hito cuando el tipo es CUSTOM");
        }
        List<Milestone> milestones = new ArrayList<>();
        BigDecimal assigned = BigDecimal.ZERO;
        for (int index = 0; index < specs.size(); index++) {
            MilestoneSpec spec = specs.get(index);
            if (spec.amount() == null || spec.amount().signum() <= 0) {
                throw new ValidationException("El monto de cada hito debe ser mayor a cero");
            }
            assigned = assigned.add(spec.amount());
            milestones.add(Milestone.create(contractId, spec.title(), spec.description(),
                    Money.of(spec.amount(), total.currency()), spec.dueDate(), index));
        }
        if (assigned.compareTo(total.amount()) > 0) {
            throw new ValidationException("La suma de los hitos supera el monto del contrato");
        }
        return milestones;
    }

    private LocalDate dueDate(LocalDate startDate, LocalDate endDate, int index, int totalParts) {
        if (endDate == null) {
            return null;
        }
        LocalDate start = startDate == null ? endDate.minusDays(30L * totalParts) : startDate;
        long days = ChronoUnit.DAYS.between(start, endDate);
        long offset = Math.max(0, days * index / totalParts);
        return start.plusDays(offset);
    }
}
