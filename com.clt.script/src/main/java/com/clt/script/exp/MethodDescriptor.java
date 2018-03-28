package com.clt.script.exp;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.clt.script.exp.values.Undefined;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class MethodDescriptor extends FunctionDescriptor implements Comparable<MethodDescriptor> {

    private Method method;
    private boolean isStatic;
    private boolean isVarArg;

    public MethodDescriptor(Method method, boolean isStatic, boolean isVarArg) {
        this.method = method;
        this.isStatic = isStatic;
        this.isVarArg = isVarArg;
    }

    @Override
    public Type getReturnType() {
        Class<?> returnType = this.method.getReturnType();
        if (returnType == Void.TYPE) {
            return Type.Void;
        } else {
            return Type.getTypeForClass(this.method.getReturnType());
        }
    }

    @Override
    public int hashCode() {
        int hash = this.method.getName().hashCode();
        Class<?>[] parmtypes = this.method.getParameterTypes();
        for (int i = 0; i < parmtypes.length; i++) {
            hash = hash ^ parmtypes.hashCode();
        }
        return hash;
    }

    public int compareTo(MethodDescriptor m) {
        int r = this.getName().compareTo(m.getName());
        if (r == 0) {
            Class<?>[] p1 = this.getParameterClasses();
            Class<?>[] p2 = m.getParameterClasses();
            r = p1.length - p2.length;
            for (int i = 0; (r == 0) && (i < p1.length); i++) {
                r = p1[i].getName().compareTo(p2[i].getName());
            }
        }
        return r;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MethodDescriptor) {
            MethodDescriptor m = (MethodDescriptor) o;
            if (!this.getName().equals(m.getName())) {
                return false;
            }

            Class<?> p1[] = this.getParameterClasses();
            Class<?> p2[] = m.getParameterClasses();

            if (p1.length != p2.length) {
                return false;
            }
            for (int i = 0; i < p1.length; i++) {
                if (p1[i] != p2[i]) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean match(boolean isStatic, String name, Value[] values) {
        Class<?>[] c = new Class[values.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = values[i].getClass();
        }
        return this.match(isStatic, name, c);
    }

    public boolean match(boolean isStatic, String name, Type[] types) {
        Class<?>[] c = new Class[types.length];
        for (int i = 0; i < c.length; i++) {
            c[i] = types[i].getObjectClass();
        }
        return this.match(isStatic, name, c);
    }

    private boolean match(boolean isStatic, String name, Class<?>[] types) {
        if (isStatic && !this.isStatic) {
            return false;
        }

        if (!name.equals(this.getName())) {
            return false;
        }

        Class<?> p[] = this.getParameterClasses();
        if (this.isVarArg() ? types.length < p.length - 1
                : types.length != p.length) {
            return false;
        }
        for (int i = 0; i < p.length; i++) {
            if (this.isVarArg() && (i == p.length - 1)) {
                Class<?> c = p[i].getComponentType();
                for (int j = i; j < types.length; j++) {
                    if (!((types[j] == Value.class) || c.isAssignableFrom(types[j]))) {
                        return false;
                    }
                }
            } else {
                if (!((types[i] == Value.class) || p[i].isAssignableFrom(types[i]))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return this.method.getName();
    }

    public Class<?>[] getParameterClasses() {
        return this.method.getParameterTypes();
    }

    @Override
    public Type[] getParameterTypes() {
        Class<?>[] c = this.getParameterClasses();
        Type[] t = new Type[this.isVarArg() ? c.length - 1 : c.length];
        for (int i = 0; i < t.length; i++) {
            t[i] = Type.getTypeForClass(c[i]);
        }
        return t;
    }

    public Class<?> getDeclaringClass() {
        return this.method.getDeclaringClass();
    }

    @Override
    public boolean isVarArg() {
        return this.isVarArg;
    }

    public Value eval(Value[] args) {
        return this.eval(null, args);
    }

    public Value eval(Object caller, Value[] args) {
        if ((caller == null) && !this.isStatic) {
            throw new EvaluationException("Non-static function " + this
                    + " invoked without caller.");
        }

        if ((caller != null)
                && !this.method.getDeclaringClass().isAssignableFrom(caller.getClass())) {
            throw new EvaluationException("Calling "
                    + this.method.getDeclaringClass() + "."
                    + this.method.getName() + " with caller " + caller
                    + " of class "
                    + caller.getClass());
        }

        try {
            Object o;
            if (this.isVarArg()) {
                Class<?> c[] = this.getParameterClasses();
                Object arguments[] = new Object[c.length];
                System.arraycopy(args, 0, arguments, 0, arguments.length - 1);
                Value[] varargs
                        = (Value[]) Array.newInstance(c[c.length - 1].getComponentType(),
                                args.length - c.length + 1);
                System.arraycopy(args, c.length - 1, varargs, 0, varargs.length);
                arguments[arguments.length - 1] = varargs;
                o = this.method.invoke(caller, arguments);
            } else {
                o = this.method.invoke(caller, (Object[]) args);
            }
            if (o == null) {
                if (this.method.getReturnType() == Void.TYPE) {
                    return Value.Void;
                } else {
                    return new Undefined();
                }
            } else {
                return (Value) o;
            }
        } catch (EvaluationException exn) {
            throw exn;
        } catch (InvocationTargetException exn) {
            Throwable cause = exn.getTargetException();
            if (cause instanceof EvaluationException) {
                throw (EvaluationException) cause;
            } else {
                throw new EvaluationException(cause.toString());
            }
        } catch (IllegalArgumentException exn) {
            StringBuilder b = new StringBuilder();
            b.append("Wrong type of arguments: ");
            b.append(this.getName());
            b.append('(');
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    b.append(", ");
                }
                b.append(args[i]);
            }
            b.append(')');
            throw new EvaluationException(b.toString());
        } catch (Exception exn) {
            throw new EvaluationException(exn.toString());
        }
    }

}
