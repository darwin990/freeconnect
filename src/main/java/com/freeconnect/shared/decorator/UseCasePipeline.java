package com.freeconnect.shared.decorator;

import com.freeconnect.shared.application.UseCase;
import java.util.Objects;

/**
 * Composicion fluida de decoradores de casos de uso.
 *
 * <p><b>Patron: Decorator.</b> El orden de envoltura es: logging -&gt; autorizacion
 * -&gt; auditoria -&gt; caso de uso real, de modo que un fallo de autorizacion no
 * genera registros de auditoria de negocio.</p>
 */
public final class UseCasePipeline<I, O> {

    private final UseCase<I, O> current;

    private UseCasePipeline(UseCase<I, O> current) {
        this.current = current;
    }

    public static <I, O> UseCasePipeline<I, O> around(UseCase<I, O> core) {
        return new UseCasePipeline<>(Objects.requireNonNull(core));
    }

    public UseCasePipeline<I, O> withLogging(String name) {
        return new UseCasePipeline<>(new LoggingUseCaseDecorator<>(current, name));
    }

    public UseCasePipeline<I, O> withAuthorization(java.util.function.Predicate<I> policy, String description) {
        return new UseCasePipeline<>(new AuthorizationDecorator<>(current, policy, description));
    }

    public UseCase<I, O> build() {
        return current;
    }
}
