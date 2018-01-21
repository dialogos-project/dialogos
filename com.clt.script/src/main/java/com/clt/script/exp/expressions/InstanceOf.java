package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class InstanceOf extends Expression {

    private Expression exp;
    private String type;

    public InstanceOf(Expression exp, String type) {

        this.exp = exp;
        this.type = type;
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        return new InstanceOf(this.exp.copy(mapping), this.type);
    }

    @Override
    public Type getType() {

        this.exp.getType();
        Type.getTypeForName(this.type);
        return Type.Bool;
    }

    @Override
    protected Value eval(Debugger dbg) {

        Type vt = this.exp.evaluate(dbg).getType();
        Type t = Type.getTypeForName(this.type);

        return new BoolValue(vt.getObjectClass() == t.getObjectClass());
    }

    @Override
    public int getPriority() {

        return 9;
    }

    @Override
    public void write(PrintWriter w) {

        this.exp.write(w, this.exp.getPriority() < this.getPriority());
        w.print(" instanceof ");
        w.print(this.type);
    }

}
