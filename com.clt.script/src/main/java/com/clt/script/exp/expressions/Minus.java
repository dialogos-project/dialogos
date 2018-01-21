package com.clt.script.exp.expressions;

import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.RealValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class Minus extends BinaryOperator {

    public Minus(Expression e1, Expression e2) {

        super("-", BinaryOperator.LEFT, e1, e2);
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        return new Minus(this.e1.copy(mapping), this.e2.copy(mapping));
    }

    @Override
    protected Value eval(Debugger dbg) {

        Value v1 = this.e1.evaluate(dbg);
        Value v2 = this.e2.evaluate(dbg);

        if ((v1 instanceof IntValue) && (v2 instanceof IntValue)) {
            return (new IntValue(((IntValue) v1).getInt() - ((IntValue) v2).getInt()));
        } else if ((v1 instanceof RealValue) && (v2 instanceof RealValue)) {
            return (new RealValue(((RealValue) v1).getReal()
                    - ((RealValue) v2).getReal()));
        } else if ((v1 instanceof RealValue) && (v2 instanceof IntValue)) {
            return (new RealValue(((RealValue) v1).getReal() - ((IntValue) v2).getInt()));
        } else if ((v1 instanceof IntValue) && (v2 instanceof RealValue)) {
            return (new RealValue(((IntValue) v1).getInt() - ((RealValue) v2).getReal()));
        } else {
            throw new EvaluationException("Illegal arguments: can't evaluate " + v1
                    + " - " + v2);
        }
    }

    @Override
    public Type getType() {

        Type t1 = this.e1.getType();
        Type t2 = this.e2.getType();

        if ((t1.equals(Type.Int) || t1.equals(Type.Real))
                && (t2.equals(Type.Int) || t2.equals(Type.Real))) {
            if (t1.equals(Type.Real) || t2.equals(Type.Real)) {
                return Type.Real;
            } else {
                return Type.Int;
            }
        } else {
            Type t = Type.unify(t1, t2);
            if (t.equals(Type.Int) || t.equals(Type.Real)
                    || (t instanceof TypeVariable)) {
                return t;
            } else {
                throw new TypeException(
                        "Type mismatch in arguments of binary operator -");
            }
        }
    }

    @Override
    public int getPriority() {

        return 10;
    }
}
