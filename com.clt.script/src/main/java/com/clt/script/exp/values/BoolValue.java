package com.clt.script.exp.values;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

/**
 * A boolean value being <code>true</code> or <code>false</code>.
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public final class BoolValue extends PrimitiveValue {

    public static final BoolValue 
            TRUE = new BoolValue(true), 
            FALSE = new BoolValue(false);

    private boolean value;

    public BoolValue(boolean value) {

        this.value = value;
    }

    /**
     * Return the native value of this BoolValue as a boolean.
     */
    public boolean getBool() {

        return this.value;
    }

    @Override
    protected Value copyValue() {

        return new BoolValue(this.value);
    }

    @Override
    public Type getType() {

        return Type.Bool;
    }

    @Override
    public boolean equals(Object v) {

        if (v instanceof BoolValue) {
            return ((BoolValue) v).getBool() == this.getBool();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        return this.value ? 1231 : 1237;
    }

    @Override
    public String toString() {

        return this.value ? "true" : "false";
    }

    public static BoolValue valueOf(String s) {

        return new BoolValue(s.equals("true"));
    }

    @Override
    public Object getReadableValue() {
        return getBool();
    }
}
