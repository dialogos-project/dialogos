package com.clt.script.exp.types;

import com.clt.script.exp.Type;
import com.clt.script.exp.values.PointerValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class PointerType extends Type {

    private Type baseType;

    public PointerType(Type baseType) {

        this.baseType = baseType;
    }

    @Override
    public Object clone() {

        return new PointerType((Type) this.baseType.clone());
    }

    public Type getBaseType() {

        Type rType = this.baseType.resolve();
        if (rType != this.baseType) {
            this.baseType = rType;
        }
        return this.baseType;
    }

    @Override
    public boolean equals(Object type) {

        if (type instanceof PointerType) {
            return this.getBaseType().equals(((PointerType) type).getBaseType());
        } else {
            return false;
        }
    }

    @Override
    protected Type unify(Type type) {

        if (type instanceof PointerType) {
            PointerType t = (PointerType) type;
            Type.unify(this.getBaseType(), t.getBaseType());
            return this;
        } else {
            return this.throwUnificationException(type);
        }
    }

    @Override
    public String toString() {

        return "*" + this.baseType.toString();
    }

    @Override
    public Class<PointerValue> getObjectClass() {

        return PointerValue.class;
    }

    @Override
    public java.lang.String getName() {
        return "pointer";
    }
}
