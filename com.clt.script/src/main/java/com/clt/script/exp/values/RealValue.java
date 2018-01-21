package com.clt.script.exp.values;

import com.clt.script.exp.Type;
import com.clt.script.exp.Value;

/**
 * An double precision floating point value.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public final class RealValue extends PrimitiveValue implements Comparable<RealValue> {

    private double value;

    public RealValue(double value) {

        this.value = value;
    }

    /**
     * Return the native value of this RealValue as a double.
     */
    public double getReal() {

        return this.value;
    }

    @Override
    protected Value copyValue() {

        return new RealValue(this.value);
    }

    @Override
    public Type getType() {

        return Type.Real;
    }

    @Override
    public boolean equals(Object v) {

        if (v instanceof RealValue) {
            return ((RealValue) v).getReal() == this.getReal();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        long bits = Double.doubleToLongBits(this.value);
        return (int) (bits ^ (bits >>> 32));
    }

    public int compareTo(RealValue o) {

        double v = o.getReal();
        if (this.value == v) {
            return 0;
        } else if (this.value < v) {
            return -1;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {

        return String.valueOf(this.value);
    }

    public static RealValue valueOf(String s) {

        return new RealValue(Double.parseDouble(s));
    }

    @Override
    public Object getReadableValue() {
        return getReal();
    }

}
