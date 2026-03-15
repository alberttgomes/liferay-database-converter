package com.upgrade.tools.util.function;

/**
 * @author Albert Gomes Cabral
 * @param <T> Entity
 * @param <R> Result
 * @param <E> Custom Exception
 */
@FunctionalInterface
public interface UnsafeFunction <T, R, E extends Throwable> {

    R apply(T t) throws E;

}
