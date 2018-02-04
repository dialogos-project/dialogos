package com.clt.diamant;

/**
 * Exception class that should be used for Exceptions that occur while logging
 *
 * @author Till Kollenda
 */
public class LoggingException extends RuntimeException {

    /**
     *
     * @param message exception message
     * @param e the throwable that caused this exception
     */
    public LoggingException(String message, Throwable e) {
        super("\n" + message, e);
    }
}
