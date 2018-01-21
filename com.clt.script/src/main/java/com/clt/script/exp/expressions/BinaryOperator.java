package com.clt.script.exp.expressions;

import java.io.PrintWriter;

import com.clt.script.exp.Expression;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
abstract class BinaryOperator extends Expression {

    protected static final int LEFT = 0;
    protected static final int RIGHT = 1;
    protected static final int NONE = 2;

    protected String op;
    protected int associativity;
    protected Expression e1, e2;

    public BinaryOperator(String op, int associativity, Expression e1, Expression e2) {
        this.op = op;
        this.associativity = associativity;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void write(PrintWriter w) {

        this.e1.write(w, this.associativity == BinaryOperator.LEFT
                ? this.e1.getPriority() < this.getPriority()
                : this.e1.getPriority() <= this.getPriority());
        w.print(' ');
        w.print(this.op);
        w.print(' ');
        this.e2.write(w, this.associativity == BinaryOperator.RIGHT
                ? this.e2.getPriority() < this.getPriority()
                : this.e2.getPriority() <= this.getPriority());
    }
}
