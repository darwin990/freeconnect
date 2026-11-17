package com.freeconnect.projects.domain;

/**
 * Contrato de clonacion de plantillas.
 *
 * <p><b>Patron: Prototype.</b> La copia es profunda para que modificar la plantilla
 * resultante no afecte al original registrado.</p>
 */
public interface Prototype<T> {

    T copy();
}
