package com.clt.script.exp;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class TypeException extends RuntimeException {

    public TypeException() {

        super();
    }

    public TypeException(String s) {

        super(s);
    }

    @Override
    public String toString() {

        return "Type error: " + this.getLocalizedMessage();
    }
}
