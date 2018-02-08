package com.clt.lego;

import java.io.IOException;

/**
 * @author dabo
 *
 */
public class BrickException extends IOException {

    public BrickException(String message) {

        super(message);
    }

    public BrickException(String message, Throwable cause) {

        this(message);

        this.initCause(cause);
    }
}
