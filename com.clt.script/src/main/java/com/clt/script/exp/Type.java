package com.clt.script.exp;

import java.util.Hashtable;
import java.util.Map;

import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.StructType;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;

/**
 * Base class for all static types.
 *
 * The static type of an Expression gives information, what type of
 * {@link Value} will result from the evaluation of the Expression.
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public abstract class Type {

    public static final Type Any = new TypeVariable(true);
    public static final Type Bool = new PrimitiveType("bool", BoolValue.class);
    public static final Type Int = new PrimitiveType("int", IntValue.class);
    public static final Type Real = new PrimitiveType("real", RealValue.class);
    public static final Type String = new PrimitiveType("string", StringValue.class);
    public static final Type Void = new PrimitiveType("void", Value.Void.getClass());

    private static Map<String, Type> DATATYPES;

    static {
        Type.DATATYPES = new Hashtable<String, Type>();
        Type.DATATYPES.put("any", Type.Any);
        Type.DATATYPES.put("bool", Type.Bool);
        Type.DATATYPES.put("int", Type.Int);
        Type.DATATYPES.put("real", Type.Real);
        Type.DATATYPES.put("string", Type.String);
        Type.DATATYPES.put("void", Type.Void);
        Type.DATATYPES.put("list", new ListType());
        Type.DATATYPES.put("struct", new StructType());
    }

    protected Type() {

    }

    public abstract Class<? extends Value> getObjectClass();

    @Override
    public abstract boolean equals(Object type);

    protected abstract Type unify(Type t);

    @Override
    public abstract Object clone();

    public Type copy() {

        return (Type) this.clone();
    }

    public Type resolve() {

        return this;
    }

    protected Type throwUnificationException(Type t) {

        throw new TypeException("Can't unify types \"" + this + "\" and \"" + t
                + "\".");
    }

    public static Type createType(String name, Class<? extends Value> objectClass) {

        if (Type.DATATYPES.containsKey(name)) {
            throw new IllegalArgumentException("Redefinition of primitive type '"
                    + name + "'");
        }
        Type t = new PrimitiveType(name, objectClass);
        Type.DATATYPES.put(name, t);
        return t;
    }

    public static void registerType(String name, Type t) {

        if (Type.DATATYPES.containsKey(name)) {
            throw new IllegalArgumentException("Redefinition of type '" + name + "'");
        }
        Type.DATATYPES.put(name, t);
    }

    public static Type getTypeForName(String name) {

        return Type.DATATYPES.get(name);
    }

    public static Type getTypeForClass(Class<?> clss) {

        for (Type t : Type.DATATYPES.values()) {
            if ((t != Type.Any) && t.getObjectClass().isAssignableFrom(clss)) {
                return t;
            }
        }
        return new TypeVariable();
    }

    public static boolean equals(Type t1, Type t2) {

        t1 = t1.resolve();
        t2 = t2.resolve();

        if (t1 == t2) {
            return true;
        } else {
            return t1.equals(t2);
        }
    }

    public static Type unify(Type t1, Type t2) {

        t1 = t1.resolve();
        t2 = t2.resolve();

        if (t1 == t2) {
            return t1;
        } else if (t2 instanceof TypeVariable) {
            return t2.unify(t1);
        } else {
            return t1.unify(t2);
        }
    }

    private static class PrimitiveType
            extends Type {

        String name;
        Class<? extends Value> objectClass;

        public PrimitiveType(String name, Class<? extends Value> objectClass) {

            this.name = name;
            this.objectClass = objectClass;
        }

        @Override
        public boolean equals(Object type) {

            return this == type;
        }

        @Override
        protected Type unify(Type t) {

            if (this.equals(t)) {
                return this;
            } else {
                return this.throwUnificationException(t);
            }
        }

        @Override
        public String toString() {

            return this.name;
        }

        @Override
        public Class<? extends Value> getObjectClass() {

            return this.objectClass;
        }

        @Override
        public Object clone() {

            return this;
        }
    }
}
