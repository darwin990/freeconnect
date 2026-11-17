package com.freeconnect.messaging.domain.state;

import com.freeconnect.messaging.domain.ConversationStatus;
import java.util.Map;
import java.util.Set;

/**
 * Registro inmutable de estados de conversacion.
 */
public final class ConversationStates {

    private static final Map<ConversationStatus, ConversationState> REGISTRY = Map.of(
            ConversationStatus.ACTIVE, new SimpleConversationState(ConversationStatus.ACTIVE,
                    Set.of(ConversationStatus.ARCHIVED)),
            ConversationStatus.ARCHIVED, new SimpleConversationState(ConversationStatus.ARCHIVED, Set.of()));

    private ConversationStates() {
    }

    public static ConversationState of(ConversationStatus status) {
        return REGISTRY.get(status);
    }

    private record SimpleConversationState(ConversationStatus status, Set<ConversationStatus> allowedTransitions)
            implements ConversationState {
    }
}
