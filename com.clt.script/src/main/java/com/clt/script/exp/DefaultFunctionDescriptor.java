package com.clt.script.exp;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DefaultFunctionDescriptor extends FunctionDescriptor {

    String name;
    Type returnType;
    Type[] paramTypes;
    boolean varArg;

    public DefaultFunctionDescriptor(String name, Type returnType, Type[] paramTypes) {
        this(name, returnType, paramTypes, false);
    }

    public DefaultFunctionDescriptor(String name, Type returnType, Type[] paramTypes, boolean varArg) {
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        this.varArg = varArg;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public Type getReturnType() {

        return this.returnType;
    }

    @Override
    public Type[] getParameterTypes() {

        return this.paramTypes;
    }

    @Override
    public boolean isVarArg() {

        return this.varArg;
    }
}
