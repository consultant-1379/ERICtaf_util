package com.ericsson.cifwk.taf.ddc;

public class DDCHandlerException extends RuntimeException {
    public DDCHandlerException(String message, Object... args) {
        super(Messages.format(message, args));
    }

    public DDCHandlerException(String message, Throwable cause, Object... args) {
        super(Messages.format(message, args), cause);
    }
}

