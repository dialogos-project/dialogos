package com.clt.util;

/**
 * A <code>Timeout</code> indicates that the timeout limit wait()ing for an
 * operation (e.g. retrieval from a Queue) has been reached.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Timeout extends Exception {

    public Timeout() {

        super();
    }

    public Timeout(String message) {

        super(message);
    }
}
