package com.clt.script.parser;

/**
 * @author dabo
 *
 */
public class ParseException extends Exception {

    public ParseException(String s) {

        super(s);
    }

    @Override
    public String toString() {

        return this.getLocalizedMessage();
    }
}
