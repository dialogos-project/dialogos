package com.clt.script.exp.expressions;

import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.MethodDescriptor;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 3.0
 */
public class NativeFunction extends Function {

    Object caller;
    MethodDescriptor[] methods;

    public NativeFunction(MethodDescriptor methods[], Expression[] arguments) {

        this(null, methods, arguments);
    }

    public NativeFunction(Object caller, MethodDescriptor methods[],
            Expression[] arguments) {

        super(methods[0].getName(), arguments);
        this.caller = caller;
        this.methods = methods;

        if (caller != null) {
            for (int i = 0; i < methods.length; i++) {
                if (!methods[i].getDeclaringClass().isAssignableFrom(caller.getClass())) {
                    throw new IllegalArgumentException("Can't call a method from class "
                            + methods[i].getDeclaringClass()
                            + " with a caller of class " + caller.getClass());
                }
            }
        }
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        Expression args[] = new Expression[this.numArguments()];
        for (int i = 0; i < args.length; i++) {
            args[i] = this.getArgument(i).copy(mapping);
        }

        return new NativeFunction(this.caller, this.methods, args);
    }

    @Override
    protected Value eval(Debugger dbg, Value[] args) {

        for (int i = 0; i < this.methods.length; i++) {
            if (this.methods[i].match(this.caller == null, this.methods[0].getName(),
                    args)) {
                return this.methods[i].eval(this.caller, args);
            }
        }
        // throw an exception
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
    }

    @Override
    public Type getType() {

        Type t = this.methods[0].getReturnType();
        for (int i = 1; i < this.methods.length; i++) {
            if (!Type.equals(t, this.methods[i].getReturnType())) {
                return new TypeVariable();
            }
        }
        return t;
    }

}
