package com.clt.script.exp.values;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;

/**
 * Undefined is a <code>null</code>-value that matches any type.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Undefined extends PrimitiveValue {

    private Type type;

    public Undefined() {

        this.type = new TypeVariable();
    }

    @Override
    protected Value copyValue() {

        return new Undefined();
    }

    @Override
    public Type getType() {

        return this.type;
    }

    @Override
    public boolean equals(Object v) {

        return v instanceof Undefined;
    }

    @Override
    public int hashCode() {

        return 0x0815;
    }

    @Override
    public String toString() {

        return "undefined";
    }

    @Override
    public Object getReadableValue() {
        return null;
    }
}
