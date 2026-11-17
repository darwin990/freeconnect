package com.freeconnect.messaging.domain.state;

import com.freeconnect.messaging.domain.ConversationStatus;
import java.util.Set;

/**
 * Estado de conversacion.
 *
 * <p><b>Patron: State.</b> Las transiciones validas se encapsulan por estado.</p>
 */
public interface ConversationState {

    ConversationStatus status();

    Set<ConversationStatus> allowedTransitions();

    default boolean canTransitionTo(ConversationStatus target) {
        return allowedTransitions().contains(target);
    }
}
