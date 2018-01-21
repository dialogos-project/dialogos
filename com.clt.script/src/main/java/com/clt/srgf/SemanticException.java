package com.clt.srgf;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class SemanticException extends RuntimeException {

    private Grammar grammar;

    public SemanticException(Grammar grammar) {

        super();

        if (grammar == null) {
            throw new IllegalArgumentException();
        }
        this.grammar = grammar;
    }

    public SemanticException(Grammar grammar, String s) {

        super(s);

        if (grammar == null) {
            throw new IllegalArgumentException();
        }
        this.grammar = grammar;
    }

    @Override
    public final String getMessage() {

        String prefix = "";
        if ((this.grammar != null) && (this.grammar.getName() != null)) {
            prefix = "Error in grammar " + this.grammar.getName() + ": ";
        }
        return prefix + super.getMessage();
    }
}
