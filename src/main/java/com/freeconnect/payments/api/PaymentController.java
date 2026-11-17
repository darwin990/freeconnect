package com.freeconnect.payments.api;

import com.freeconnect.payments.application.PaymentUseCases;
import com.freeconnect.payments.application.PaymentView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de pagos de contratos.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Pagos", description = "Liberacion de pagos y comisiones")
public class PaymentController {

    private final PaymentUseCases useCases;

    public PaymentController(PaymentUseCases useCases) {
        this.useCases = useCases;
    }

    @PostMapping("/contracts/{id}/payments")
    @PreAuthorize("hasRole('CLIENT')")
    @Operation(summary = "Liberar un pago del contrato (Adapter de pasarela)")
    public ResponseEntity<PaymentView> release(@PathVariable UUID id,
                                               @Valid @RequestBody ReleasePaymentRequest request) {
        PaymentView payment = useCases.release(id, request.amount());
        return ResponseEntity.created(URI.create("/api/v1/payments/" + payment.id())).body(payment);
    }

    @GetMapping("/contracts/{id}/payments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Historial de pagos del contrato")
    public List<PaymentView> byContract(@PathVariable UUID id) {
        return useCases.byContract(id);
    }

    @GetMapping("/payments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mis pagos como pagador o beneficiario")
    public List<PaymentView> mine() {
        return useCases.mine();
    }

    /**
     * Monto a liberar (opcional: por defecto el total del contrato).
     */
    public record ReleasePaymentRequest(@DecimalMin("0.01") BigDecimal amount) {
    }
}
