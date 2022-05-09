package com.scb.settlement.exception;

public class S1BatchRunFailureException extends RuntimeException {

    public S1BatchRunFailureException() {
        super();
    }

    public S1BatchRunFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public S1BatchRunFailureException(final String message) {
        super(message);
    }

    public S1BatchRunFailureException(final Throwable cause) {
        super(cause);
    }
}