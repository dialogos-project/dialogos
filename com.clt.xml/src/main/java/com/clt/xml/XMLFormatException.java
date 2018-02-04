package com.clt.xml;

import java.io.IOException;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class XMLFormatException extends IOException {

    public XMLFormatException(String message) {

        super(message);
    }

    public XMLFormatException(String message, Throwable cause) {

        this(message);

        this.initCause(cause);
    }
}
