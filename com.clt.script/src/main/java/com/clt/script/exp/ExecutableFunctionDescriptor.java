package com.clt.script.exp;

public abstract class ExecutableFunctionDescriptor extends DefaultFunctionDescriptor {
    public ExecutableFunctionDescriptor(String name, Type returnType, Type[] paramTypes) {
        super(name, returnType, paramTypes);
    }

    public abstract Value eval(Value[] args);
}
