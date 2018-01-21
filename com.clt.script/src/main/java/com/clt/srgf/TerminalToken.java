package com.clt.srgf;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
class TerminalToken extends Token<String> {

    private String pattern;

    public TerminalToken(String pattern) {

        super(pattern.intern());
        this.pattern = pattern;
    }

    @Override
    public boolean match(Input input) {

        if (!input.isEmpty()) {
            return this.pattern.equals(input.getFirstPattern());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {

        return this.pattern;
    }
}
