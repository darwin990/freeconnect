package com.freeconnect.messaging.api;

import com.freeconnect.messaging.application.ConversationView;
import com.freeconnect.messaging.application.MessagingUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
 * API de conversaciones y mensajes (RF-06).
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Mensajeria", description = "Conversaciones por contrato y mensajes")
public class MessagingController {

    private final MessagingUseCases useCases;

    public MessagingController(MessagingUseCases useCases) {
        this.useCases = useCases;
    }

    @PostMapping("/contracts/{contractId}/conversations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Abrir (o recuperar) la conversacion del contrato")
    public ResponseEntity<ConversationView> open(@PathVariable UUID contractId) {
        ConversationView conversation = useCases.open(contractId);
        return ResponseEntity.created(URI.create("/api/v1/conversations/" + conversation.id())).body(conversation);
    }

    @GetMapping("/conversations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar mis conversaciones")
    public List<ConversationView> list() {
        return useCases.listMine();
    }

    @GetMapping("/conversations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Detalle de una conversacion con mensajes")
    public ConversationView get(@PathVariable UUID id) {
        return useCases.get(id);
    }

    @PostMapping("/conversations/{id}/messages")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Enviar un mensaje (emite MessageSentEvent)")
    public ConversationView send(@PathVariable UUID id, @Valid @RequestBody SendMessageRequest request) {
        return useCases.send(id, request.body());
    }

    @PostMapping("/conversations/{id}/read")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marcar como leidos los mensajes recibidos")
    public ConversationView markRead(@PathVariable UUID id) {
        return useCases.markRead(id);
    }

    @PostMapping("/conversations/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archivar una conversacion (State)")
    public ConversationView archive(@PathVariable UUID id) {
        return useCases.archive(id);
    }

    /**
     * Cuerpo de un mensaje.
     */
    public record SendMessageRequest(@NotBlank @Size(max = 4000) String body) {
    }
}
