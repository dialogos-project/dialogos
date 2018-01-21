package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.ListValue;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 2.0
 */
public class ListExpression extends Expression {

    private List<Expression> expressions;

    public ListExpression(Expression[] expressions) {

        this(Arrays.asList(expressions));
    }

    public ListExpression(List<Expression> expressions) {

        this.expressions = expressions;
    }

    @Override
    public Expression copy(Map<?, ?> mapping) {

        List<Expression> elems = new ArrayList<Expression>(this.expressions.size());
        for (Iterator<Expression> it = this.expressions.iterator(); it.hasNext();) {
            elems.add(it.next().copy(mapping));
        }
        return new ListExpression(elems);
    }

    @Override
    protected Value eval(Debugger dbg) {

        Value[] l = new Value[this.expressions.size()];
        for (int i = 0; i < this.expressions.size(); i++) {
            l[i] = (this.expressions.get(i).evaluate(dbg));
        }
        return new ListValue(l);
    }

    @Override
    public Type getType() {

        Type elementType = new TypeVariable();
        for (Iterator<Expression> it = this.expressions.iterator(); it.hasNext();) {
            elementType = Type.unify(elementType, it.next().getType());
        }
        return new ListType(elementType);
    }

    @Override
    public int getPriority() {

        return Integer.MAX_VALUE;
    }

    @Override
    public void write(PrintWriter w) {

        w.print("[ ");
        for (Iterator<Expression> it = this.expressions.iterator(); it.hasNext();) {
            it.next().write(w);
            if (it.hasNext()) {
                w.print(", ");
            } else {
                w.print(' ');
            }
        }
        w.print("]");
    }
}
