package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public abstract class Function extends Expression {

    private String name;
    private Expression[] arguments;

    public Function(String name, Expression[] arguments) {

        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {

        return this.name;
    }

    public Expression[] getArguments() {

        return this.arguments;
    }

    public int numArguments() {

        return this.arguments.length;
    }

    public Expression getArgument(int index) {

        return this.arguments[index];
    }

    @Override
    protected Value eval(Debugger dbg) {

        Value args[] = new Value[this.numArguments()];
        for (int i = 0; i < args.length; i++) {
            args[i] = this.getArgument(i).evaluate().copy();
        }
        return this.eval(dbg, args);
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        Expression args[] = new Expression[this.arguments.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = this.arguments[i].copy(mapping);
        }
        try {
            return this.getClass().getConstructor(
                    new Class[]{String.class, args.getClass()}).
                    newInstance(new Object[]{this.name, args});
        } catch (Exception exn) {
            throw new AbstractMethodError(
                    this.getClass().getName()
                    + " does not override copy() and has no constructor <init>(String, Expression[])");
        }
    }

    protected abstract Value eval(Debugger dbg, Value[] args);

    @Override
    public int getPriority() {

        return Integer.MAX_VALUE;
    }

    @Override
    public void write(PrintWriter w) {

        w.print(this.name);
        w.print('(');
        for (int i = 0; i < this.arguments.length; i++) {
            if (i > 0) {
                w.print(", ");
            }
            this.arguments[i].write(w);
        }
        w.print(')');
    }
}
