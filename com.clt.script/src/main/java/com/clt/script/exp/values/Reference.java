package com.clt.script.exp.values;

import com.clt.script.exp.Value;

/**
 * Common interface for classes that map strings to values.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public interface Reference {

    public Value getValue(String label);

    public void setValue(String label, Value value);
}
