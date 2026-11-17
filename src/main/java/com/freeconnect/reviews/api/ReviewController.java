package com.freeconnect.reviews.api;

import com.freeconnect.reviews.application.ReviewUseCases;
import com.freeconnect.reviews.application.ReviewView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
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
 * API de resenas de contratos completados (RF-06).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Resenas", description = "Calificaciones entre las partes de un contrato")
public class ReviewController {

    private final ReviewUseCases useCases;

    public ReviewController(ReviewUseCases useCases) {
        this.useCases = useCases;
    }

    @PostMapping("/contracts/{contractId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Resenar a la contraparte de un contrato completado")
    public ResponseEntity<ReviewView> create(@PathVariable UUID contractId,
                                             @Valid @RequestBody CreateReviewRequest request) {
        ReviewView review = useCases.create(contractId, request.rating(), request.comment());
        return ResponseEntity.created(URI.create("/api/v1/contracts/" + contractId + "/reviews")).body(review);
    }

    @GetMapping("/contracts/{contractId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar resenas de un contrato")
    public List<ReviewView> byContract(@PathVariable UUID contractId) {
        return useCases.byContract(contractId);
    }

    @GetMapping("/users/{userId}/reviews")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar resenas recibidas por un usuario")
    public List<ReviewView> byTarget(@PathVariable UUID userId) {
        return useCases.byTarget(userId);
    }

    /**
     * Payload de resena.
     */
    public record CreateReviewRequest(@Min(1) @Max(5) int rating, @Size(max = 2000) String comment) {
    }
}
