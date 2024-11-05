package com.upgrades.tool.exception;

public class DatabaseTypeException extends RuntimeException {

    public DatabaseTypeException(String message) {
        super(message);
    }

    public DatabaseTypeException(String message, Throwable cause) {
        super(message, cause);
    }

}
