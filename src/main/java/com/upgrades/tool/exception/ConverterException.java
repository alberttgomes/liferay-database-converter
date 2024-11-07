package com.upgrades.tool.exception;

/**
 * @author Albert Gomes Cabral
 */
public class ConverterException extends Exception {

    public ConverterException(Exception exception) {
        super(exception);
    }

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

}
