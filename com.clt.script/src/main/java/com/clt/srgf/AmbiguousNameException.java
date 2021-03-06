package com.clt.srgf;

import java.util.Collection;

import com.clt.util.NamedEntity;

/**
 * @author dabo
 *
 */
public class AmbiguousNameException extends SemanticException {

    public AmbiguousNameException(Grammar grammar, String name, Collection<? extends NamedEntity> matches) {

        super(grammar, AmbiguousNameException.constructMessage(name, matches));
    }

    private static String constructMessage(String name, Collection<? extends NamedEntity> matches) {

        StringBuilder b = new StringBuilder();

        b.append("Could not resolve grammar name '");
        b.append(name);
        b.append("'. Candidates are: ");
        int n = 0;
        for (NamedEntity e : matches) {
            if (e.getName() != null) {
                if (n++ > 0) {
                    b.append(", ");
                }
                b.append(e.getName());
            }
        }

        return b.toString();
    }
}
