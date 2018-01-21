package com.clt.script.exp.expressions;

import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class Xor extends BinaryOperator {

    public Xor(Expression e1, Expression e2) {

        super("^", BinaryOperator.LEFT, e1, e2);
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        return new Xor(this.e1.copy(mapping), this.e2.copy(mapping));
    }

    @Override
    protected Value eval(Debugger dbg) {

        Value v1 = this.e1.evaluate(dbg);
        Value v2 = this.e2.evaluate(dbg);

        if ((v1 instanceof IntValue) && (v2 instanceof IntValue)) {
            return new IntValue(((IntValue) v1).getInt() ^ ((IntValue) v2).getInt());
        } else if ((v1 instanceof BoolValue) && (v2 instanceof BoolValue)) {
            return new BoolValue(((BoolValue) v1).getBool()
                    ^ ((BoolValue) v2).getBool());
        } else {
            throw new EvaluationException("Illegal arguments: can't evaluate " + v1
                    + " ^ " + v2);
        }
    }

    @Override
    public Type getType() {

        Type t = Type.unify(this.e1.getType(), this.e2.getType());
        if (Type.equals(t, Type.Int) || Type.equals(t, Type.Bool)
                || (t instanceof TypeVariable)) {
            return t;
        } else {
            throw new TypeException("Illegal argument type for operator ^ : " + t);
        }
    }

    @Override
    public int getPriority() {

        return 7;
    }
}
