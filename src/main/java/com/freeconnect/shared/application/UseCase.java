package com.freeconnect.shared.application;

/**
 * Contrato base de todos los casos de uso de la capa de aplicacion.
 *
 * <p><b>Patron: Command/Template Method (contrato)</b>. Los controladores solo
 * conocen {@code UseCase}; nunca repositorios ni entidades JPA.</p>
 *
 * @param <I> entrada (comando o query)
 * @param <O> salida (DTO de aplicacion)
 */
public interface UseCase<I, O> {

    O execute(I input);
}
