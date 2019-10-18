package com.clt.script.parser;

/**
 * @author dabo
 *
 */
public class ParseException extends Exception {

    public ParseException(String s) {
        super(s);
    }

    public ParseException(String s, Throwable cause) {
        super(s, cause);
    }

    @Override
    public String toString() {
        return this.getLocalizedMessage();
    }
}
