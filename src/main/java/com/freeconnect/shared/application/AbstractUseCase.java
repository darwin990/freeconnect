package com.freeconnect.shared.application;

/**
 * Esqueleto comun de un caso de uso.
 *
 * <p><b>Patron: Template Method.</b> {@link #execute(Object)} fija el algoritmo
 * (validar -&gt; ejecutar -&gt; post-proceso) y las subclases solo implementan los
 * pasos variables {@link #validate(Object)} y {@link #handle(Object)}.</p>
 */
public abstract class AbstractUseCase<I, O> implements UseCase<I, O> {

    @Override
    public O execute(I input) {
        validate(input);
        O output = handle(input);
        afterSuccess(input, output);
        return output;
    }

    /** Validaciones de entrada propias del caso de uso. */
    protected void validate(I input) {
        // por defecto sin validacion adicional
    }

    /** Logica de aplicacion (orquesta dominio + puertos). */
    protected abstract O handle(I input);

    /** Hook posterior al exito (por ejemplo invalidar cache). */
    protected void afterSuccess(I input, O output) {
        // por defecto sin accion
    }
}
