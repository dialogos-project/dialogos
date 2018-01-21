package com.clt.resources;

/**
 * @author dabo
 *
 */
public class ResourceFormatException extends RuntimeException {

    public ResourceFormatException() {
        this("Failed to load a resource");
    }

    public ResourceFormatException(String message) {
        super(message);
    }
}
