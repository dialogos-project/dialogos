/*
 * @(#)Conditional.java
 * Created on Wed Oct 09 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.expressions;

import java.io.PrintWriter;
import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class Conditional extends Expression {

  protected Expression condition;
  protected Expression consequence;
  protected Expression alternative;


  public Conditional(Expression condition, Expression consequence,
      Expression alternative) {

    this.condition = condition;
    this.consequence = consequence;
    this.alternative = alternative;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Conditional(this.condition.copy(mapping), this.consequence
      .copy(mapping), this.alternative.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value c = this.condition.evaluate(dbg);
    if (!(c instanceof BoolValue)) {
      throw new EvaluationException("Condition <" + this.condition
        + "> of operator ? is not a boolean expression");
    }
    else if (((BoolValue)c).getBool()) {
      return this.consequence.evaluate(dbg);
    }
    else {
      return this.alternative.evaluate(dbg);
    }
  }


  @Override
  public Type getType() {

    Type t = this.condition.getType();
    try {
      Type.unify(t, Type.Bool);
    } catch (TypeException exn) {
      throw new TypeException("condition <" + this.condition
        + "> of operator ? is not a boolean expression");
    }

    Type.unify(this.condition.getType(), Type.Bool);

    return Type.unify(this.consequence.getType(), this.alternative.getType());
  }


  @Override
  public int getPriority() {

    return 2;
  }


  @Override
  public void write(PrintWriter w) {

    this.condition.write(w, this.condition.getPriority() <= this.getPriority());
    w.print(" ? ");
    this.consequence.write(w, this.consequence.getPriority() <= this
      .getPriority());
    w.print(" : ");
    this.alternative.write(w, this.alternative.getPriority() <= this
      .getPriority());
  }
}
