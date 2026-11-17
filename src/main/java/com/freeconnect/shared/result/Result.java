package com.freeconnect.shared.result;

import java.util.function.Function;

/**
 * Resultado explicito de una operacion de dominio o aplicacion.
 *
 * <p>Evita el uso de excepciones para flujos esperados y hace el contrato
 * explicito en la firma de los casos de uso.</p>
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    static <T> Result<T> ok(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> fail(String code, String message) {
        return new Failure<>(code, message);
    }

    boolean isSuccess();

    T value();

    String code();

    String message();

    default T getOrThrow(Function<String, RuntimeException> exceptionFactory) {
        if (isSuccess()) {
            return value();
        }
        throw exceptionFactory.apply(message());
    }

    record Success<T>(T value) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String code() {
            return "OK";
        }

        @Override
        public String message() {
            return "ok";
        }
    }

    record Failure<T>(String code, String message) implements Result<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T value() {
            return null;
        }
    }
}
