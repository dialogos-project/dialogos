package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Prototype {

    String name;
    Type returnType;
    Type parameterTypes[];

    Proc procedure;

    public Prototype(String name, Type returnType, Type[] parameterTypes) {

        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {

        return this.name;
    }

    public Type getReturnType() {

        return this.returnType;
    }

    public int numParameters() {

        return this.parameterTypes.length;
    }

    public Type getParameterType(int index) {

        return this.parameterTypes[index];
    }

    public Type[] getParameterTypes() {

        return this.parameterTypes;
    }

    public Proc getProcedure() {

        return this.procedure;
    }

    public void setProcedure(Proc procedure) {

        if (!this.name.equals(procedure.getName())) {
            this.throwImplementationException(procedure);
        }
        if (!Type.equals(this.returnType, procedure.getReturnType())) {
            this.throwImplementationException(procedure);
        }
        Type[] parameterTypes = procedure.getParameterTypes();
        if (parameterTypes.length != this.parameterTypes.length) {
            this.throwImplementationException(procedure);
        }
        for (int i = 0; i < parameterTypes.length; i++) {
            if (!Type.equals(parameterTypes[i], this.parameterTypes[i])) {
                this.throwImplementationException(procedure);
            }
        }
        this.procedure = procedure;
    }

    public void check(Collection<String> warnings) {

        if (this.procedure == null) {
            throw new TypeException("Procedure " + this
                    + " defined but not implemented.");
        } else {
            this.procedure.check(warnings);
        }
    }

    @Override
    public String toString() {

        Type[] ptypes = this.getParameterTypes();
        StringBuilder b = new StringBuilder();
        b.append(this.getReturnType());
        b.append(' ');
        b.append(this.getName());
        b.append("(");
        for (int i = 0; i < ptypes.length; i++) {
            if (i > 0) {
                b.append(", ");
            }
            b.append(ptypes[i]);
        }
        b.append(')');
        return b.toString();
    }

    private void throwImplementationException(Proc proc) {

        throw new TypeException(
                "Implementation of function does not match definition: " + proc
                + "\nWas defined before as: " + this);
    }
}
