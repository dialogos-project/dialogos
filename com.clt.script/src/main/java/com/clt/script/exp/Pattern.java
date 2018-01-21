package com.clt.script.exp;

import java.util.HashSet;
import java.util.Map;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public interface Pattern {

    /**
     * Return a set of free variable names in this pattern.
     */
    public VarSet getFreeVars();

    /**
     * Try to match a value against this pattern.
     *
     * @return A {@link Match} object if the match is successful, or
     * <code>null</code> otherwise
     */
    public Match match(Value v);

    /**
     * Return the type of this pattern.
     */
    public Type getType(Map<String, Type> variableTypes);

    /**
     * Return a string representation of this pattern.
     */
    public String toString();

    /**
     * A set of variable names. Elements of this set are guaranteed to be of
     * type {@link java.lang.String}. Trying to add a variable name twice will
     * result in a {@link TypeException}, because patterns may bind each
     * variable name only once.
     */
    public static class VarSet extends HashSet<String> {

        @Override
        public boolean add(String s) {

            if (this.contains(s)) {
                throw new TypeException("Duplicate binding for variable '" + s + "'");
            } else {
                return super.add(s);
            }
        }

        public String[] stringArray() {

            return this.toArray(new String[this.size()]);
        }
    }
}
