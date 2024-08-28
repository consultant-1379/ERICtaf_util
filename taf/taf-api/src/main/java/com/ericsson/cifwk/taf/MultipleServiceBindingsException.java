package com.ericsson.cifwk.taf;

/**
 * Thrown when there are multiple SPI implementations of unique service found.
 *
 * @author Kirill Shepitko kirill.shepitko@ericsson.com
 *         Date: 10/05/2016
 */
public class MultipleServiceBindingsException extends RuntimeException {

    public MultipleServiceBindingsException(String message) {
        super(message);
    }

}
