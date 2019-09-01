package com.clt.script;

import com.clt.script.exp.Expression;
import com.clt.script.exp.MethodDescriptor;
import com.clt.script.exp.Type;
import com.clt.script.exp.Variable;

import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

public class BasicEnvironment implements Environment {

    protected static ClassEnvironment builtin = null;

    public BasicEnvironment() {
        if (builtin == null)
            builtin = new ClassEnvironment(BuiltinFunctions.class);
    }

    public Type getType(String typeName) {
        return builtin.getType(typeName);
    }

    public Variable createVariableReference(String name) {
        return builtin.createVariableReference(name);
    }

    public Expression createFunctionCall(String name, Expression[] arguments) {
        return builtin.createFunctionCall(name, arguments);
    }

    public Reader include(String name)
            throws IOException {
        return builtin.include(name);
    }

    public Collection<MethodDescriptor> getMethods() {
        return builtin.getMethods();
    }

}
