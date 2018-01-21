package com.clt.script;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import com.clt.script.exp.Expression;
import com.clt.script.exp.MethodDescriptor;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;
import com.clt.script.exp.expressions.NativeFunction;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class ClassEnvironment implements Environment {

    private Collection<MethodDescriptor> methods;
    private Object caller;
    private Class<?> cls;

    public ClassEnvironment(Class<?> cls) {

        this(cls, null);
    }

    public ClassEnvironment(Class<?> cls, Object caller) {

        this.caller = caller;
        this.cls = cls;
        this.methods = new HashSet<MethodDescriptor>();

        Method[] ms = cls.getDeclaredMethods();
        for (int i = 0; i < ms.length; i++) {
            int mod = ms[i].getModifiers();
            if (Modifier.isPublic(mod)) {
                Class<?>[] parmtypes = ms[i].getParameterTypes();
                boolean allValues = Value.class.isAssignableFrom(ms[i].getReturnType())
                        || (ms[i].getReturnType() == Void.TYPE);
                boolean vararg = false;
                if (allValues) {
                    for (int j = 0; (j < parmtypes.length) && allValues; j++) {
                        if (!Value.class.isAssignableFrom(parmtypes[j])) {
                            if ((j == parmtypes.length - 1)
                                    && (new Value[0]).getClass()
                                            .isAssignableFrom(parmtypes[j])) {
                                vararg = true;
                            } else {
                                allValues = false;
                            }
                        }
                    }
                }
                if (allValues) {
                    MethodDescriptor desc
                            = new MethodDescriptor(ms[i], Modifier.isStatic(mod),
                                    vararg);
                    if (Modifier.isStatic(mod) || (caller != null)) {
                        this.methods.add(desc);
                    }
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof ClassEnvironment) {
            return (((ClassEnvironment) o).cls == this.cls)
                    && (((ClassEnvironment) o).caller == this.caller);
        } else {
            return false;
        }
    }

    public Iterator<MethodDescriptor> getMethods() {

        return this.methods.iterator();
    }

    @Override
    public int hashCode() {

        return this.cls.hashCode()
                ^ (this.caller == null ? 0 : this.caller.hashCode());
    }

    public Type getType(String typeName) {

        throw new TypeException("Unknown type: " + typeName);
    }

    public Variable createVariableReference(String id) {

        throw new TypeException("Unknown variable: " + id);
    }

    public Expression createFunctionCall(String name, Expression[] arguments) {

        Type[] argTypes = new Type[arguments.length];
        for (int i = 0; i < argTypes.length; i++) {
            argTypes[i] = arguments[i].getType().resolve();
        }

        Collection<MethodDescriptor> mds = new LinkedList<MethodDescriptor>();
        for (MethodDescriptor md : this.methods) {
            if (md.match(this.caller == null, name, argTypes)) {
                if ((this.caller == null)
                        || md.getDeclaringClass().isAssignableFrom(this.caller.getClass())) {
                    mds.add(md);
                }
            }
        }

        if (!mds.isEmpty()) {
            return new NativeFunction(this.caller, mds
                    .toArray(new MethodDescriptor[mds.size()]),
                    arguments);
        } else {
            // there was no matching method, so bail out
            StringBuilder f = new StringBuilder();
            f.append(name);
            f.append('(');
            for (int i = 0; i < argTypes.length; i++) {
                if (i > 0) {
                    f.append(", ");
                }
                f.append(argTypes[i]);
            }
            f.append(')');
            throw new Environment.NoSuchFunctionException(
                    "Unknown function or wrong number/type of arguments: "
                    + f.toString());
        }
    }

    public Reader include(String name)
            throws IOException {

        throw new FileNotFoundException("Could not find " + name);
    }
}
