package org.mhh.exception;

public class DuplicateUrlException extends RuntimeException {
    public DuplicateUrlException(String message) {
        super(message);
    }
    public DuplicateUrlException(String message, Throwable cause) {
        super(message,cause);
    }
}