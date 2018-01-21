package com.clt.io;

import java.io.IOException;

/**
 * Signals that a file could not be read because it is in the wrong format.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class FileFormatException extends IOException {

    /**
     * Construct an exception with no further information.
     */
    public FileFormatException() {
        super();
    }

    /**
     * Construct an exception with a description of the format error.
     *
     * @param message a description of the format error
     */
    public FileFormatException(String message) {
        super(message);
    }
}
