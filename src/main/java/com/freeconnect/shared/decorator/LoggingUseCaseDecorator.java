package com.freeconnect.shared.decorator;

import com.freeconnect.shared.application.UseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decorator que mide y registra la ejecucion de un caso de uso.
 *
 * <p><b>Patron: Decorator.</b> Envuelve cualquier {@link UseCase} sin modificarlo.
 * Nunca registra el contenido de la entrada para evitar filtrar datos personales
 * o credenciales.</p>
 */
public class LoggingUseCaseDecorator<I, O> implements UseCase<I, O> {

    private static final Logger log = LoggerFactory.getLogger(LoggingUseCaseDecorator.class);

    private final UseCase<I, O> delegate;
    private final String name;

    public LoggingUseCaseDecorator(UseCase<I, O> delegate, String name) {
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public O execute(I input) {
        long started = System.nanoTime();
        try {
            O result = delegate.execute(input);
            log.info("use-case={} status=OK elapsedMs={}", name, elapsedMillis(started));
            return result;
        } catch (RuntimeException ex) {
            log.warn("use-case={} status=ERROR type={} elapsedMs={}", name, ex.getClass().getSimpleName(),
                    elapsedMillis(started));
            throw ex;
        }
    }

    private long elapsedMillis(long started) {
        return (System.nanoTime() - started) / 1_000_000L;
    }
}
