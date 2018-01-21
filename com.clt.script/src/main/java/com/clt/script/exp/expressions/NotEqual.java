package com.clt.script.exp.expressions;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class NotEqual extends Equal {

    public NotEqual(Expression e1, Expression e2) {

        super(e1, e2);
        this.op = "!=";
    }

    @Override
    protected Value eval(Debugger dbg) {

        BoolValue b = (BoolValue) super.eval(dbg);
        return new BoolValue(!b.getBool());
    }
}
