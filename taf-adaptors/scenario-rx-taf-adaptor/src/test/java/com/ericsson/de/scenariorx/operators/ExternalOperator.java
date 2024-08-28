package com.ericsson.de.scenariorx.operators;

public class ExternalOperator {

    public static void throwException() {
        throw new VerySpecialExternalOperatorException();
    }

    public static final class VerySpecialExternalOperatorException extends RuntimeException {

        public static final String MESSAGE = "This is a very special external operator exception";

        private VerySpecialExternalOperatorException() {
            super(MESSAGE);
        }
    }
}
