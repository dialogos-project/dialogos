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
import com.clt.script.exp.values.RealValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class GreaterEqual extends BinaryOperator {

    public GreaterEqual(Expression e1, Expression e2) {

        super(">=", BinaryOperator.NONE, e1, e2);
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        return new GreaterEqual(this.e1.copy(mapping), this.e2.copy(mapping));
    }

    @Override
    protected Value eval(Debugger dbg) {

        Value v1 = this.e1.evaluate(dbg);
        Value v2 = this.e2.evaluate(dbg);

        if ((v1 instanceof IntValue) && (v2 instanceof IntValue)) {
            return (new BoolValue(((IntValue) v1).getInt() >= ((IntValue) v2).getInt()));
        } else if ((v1 instanceof RealValue) && (v2 instanceof IntValue)) {
            return (new BoolValue(((RealValue) v1).getReal() >= ((IntValue) v2)
                    .getInt()));
        } else if ((v1 instanceof IntValue) && (v2 instanceof RealValue)) {
            return (new BoolValue(((IntValue) v1).getInt() >= ((RealValue) v2)
                    .getReal()));
        } else if ((v1 instanceof RealValue) && (v2 instanceof RealValue)) {
            return (new BoolValue(((RealValue) v1).getReal() >= ((RealValue) v2)
                    .getReal()));
        } else {
            throw new EvaluationException("Illegal arguments: can't evaluate " + v1
                    + " >= " + v2);
        }
    }

    @Override
    public Type getType() {

        Type t = Type.unify(this.e1.getType(), this.e2.getType());

        if (t.equals(Type.Int) || t.equals(Type.Real)
                || (t instanceof TypeVariable)) {
            return Type.Bool;
        } else {
            throw new TypeException("Arguments of operator >= must be numbers");
        }
    }

    @Override
    public int getPriority() {

        return 9;
    }
}
