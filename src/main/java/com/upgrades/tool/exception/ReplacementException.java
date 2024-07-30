package com.upgrades.tool.exception;

/**
 * @author Albert Gomes Cabral
 */
public class ReplacementException extends Exception {

    public ReplacementException(Exception exception) {
        super(exception);
    }

    public ReplacementException(String message) {
        super(message);
    }

    public ReplacementException(String message, Throwable cause) {
        super(message, cause);
    }

}
