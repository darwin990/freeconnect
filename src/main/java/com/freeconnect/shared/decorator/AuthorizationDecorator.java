package com.freeconnect.shared.decorator;

import com.freeconnect.shared.application.UseCase;
import com.freeconnect.shared.exception.ForbiddenException;
import java.util.function.Predicate;

/**
 * Decorator de autorizacion declarativa.
 *
 * <p><b>Patron: Decorator + Chain.</b> Se compone alrededor del caso de uso real;
 * si la politica falla, el caso de uso no llega a ejecutarse (defensa en
 * profundidad junto con {@code @PreAuthorize} y las guardas de dominio).</p>
 */
public class AuthorizationDecorator<I, O> implements UseCase<I, O> {

    private final UseCase<I, O> delegate;
    private final Predicate<I> policy;
    private final String description;

    public AuthorizationDecorator(UseCase<I, O> delegate, Predicate<I> policy, String description) {
        this.delegate = delegate;
        this.policy = policy;
        this.description = description;
    }

    @Override
    public O execute(I input) {
        if (!policy.test(input)) {
            throw new ForbiddenException("Politica de autorizacion incumplida: " + description);
        }
        return delegate.execute(input);
    }
}
