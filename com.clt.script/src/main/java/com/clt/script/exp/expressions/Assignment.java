package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class Assignment extends Expression {

    private Variable v;
    private Expression e;

    public Assignment(Variable v, Expression e) {

        this.v = v;
        this.e = e;
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        if (mapping.containsKey(this.v)) {
            return new Assignment((Variable) mapping.get(this.v), this.e.copy(mapping));
        } else {
            return new Assignment(this.v, this.e.copy(mapping));
        }
    }

    @Override
    public Value eval(Debugger dbg) {

        Value value = this.e.evaluate(dbg);

        try {
            Type.unify(value.getType(), this.v.getType());
        } catch (Exception exn) {
            throw new EvaluationException("Attempt to assign a value of type "
                    + value.getType()
                    + " to variable " + this.v + " of type " + this.v.getType());
        }

        this.v.setValue(value.copy());
        return value;
    }

    @Override
    public Type getType() {

        return Type.unify(this.v.getType(), this.e.getType());
    }

    @Override
    public int getPriority() {

        return 1;
    }

    @Override
    public void write(PrintWriter w) {

        w.print(this.v.getName());
        w.print(" = ");
        this.e.write(w, this.e.getPriority() < this.getPriority());
    }
}
