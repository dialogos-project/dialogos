package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
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
public class IDExpression extends Expression {

    private Variable v;

    public IDExpression(Variable v) {
        this.v = v;
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        if (mapping.containsKey(this.v)) {
            return new IDExpression((Variable) mapping.get(this.v));
        } else {
            return new IDExpression(this.v);
        }
    }

    @Override
    public Type getType() {

        return this.v.getType();
    }

    public String getName() {

        return this.v.getName();
    }

    @Override
    protected Value eval(Debugger dbg) {

        return this.v.getValue();
    }

    @Override
    public int getPriority() {

        return Integer.MAX_VALUE;
    }

    @Override
    public void write(PrintWriter w) {

        w.print(this.v.getName());
    }
}
