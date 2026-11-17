package com.freeconnect.messaging.application;

import com.freeconnect.contracts.domain.Contract;
import com.freeconnect.contracts.domain.ContractRepository;
import com.freeconnect.messaging.domain.Conversation;
import com.freeconnect.messaging.domain.ConversationRepository;
import com.freeconnect.shared.exception.ForbiddenException;
import com.freeconnect.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mediador entre contratos y conversaciones.
 *
 * <p><b>Patron: Mediator.</b> Los casos de uso de mensajeria no conocen como se
 * obtienen los participantes ni como se reutiliza una conversacion existente;
 * todo ese acoplamiento queda en este mediador.</p>
 */
@Component
public class ConversationMediator {

    private final ContractRepository contractRepository;
    private final ConversationRepository conversationRepository;

    public ConversationMediator(ContractRepository contractRepository,
                                ConversationRepository conversationRepository) {
        this.contractRepository = contractRepository;
        this.conversationRepository = conversationRepository;
    }

    /**
     * Devuelve la conversacion del contrato o la abre si aun no existe.
     */
    @Transactional
    public Conversation resolveOrOpen(UUID contractId, UUID actorId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new NotFoundException("Contrato", contractId));
        if (!contract.isParticipant(actorId)) {
            throw ForbiddenException.notOwner("el contrato");
        }
        return conversationRepository.findByContract(contractId)
                .orElseGet(() -> conversationRepository.save(Conversation.open(contractId, contract.clientId(),
                        contract.freelancerId(), "Contrato: " + contract.title(), actorId)));
    }
}
