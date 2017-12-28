/*
 * @(#)Neg.java
 * Created on Wed Nov 20 2002
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
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.RealValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class Neg extends Expression {

  Expression e;


  public Neg(Expression e) {

    this.e = e;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Neg(this.e.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v = this.e.evaluate(dbg);

    if (v instanceof IntValue) {
      return (new IntValue(-((IntValue)v).getInt()));
    }
    else if (v instanceof RealValue) {
      return (new RealValue(-((RealValue)v).getReal()));
    }
    else {
      throw new EvaluationException("Illegal arguments: can't evaluate -" + v);
    }
  }


  @Override
  public Type getType() {

    Type t = this.e.getType();

    if (t.equals(Type.Int) || t.equals(Type.Real)
      || (t instanceof TypeVariable)) {
      return t;
    }
    else {
      throw new TypeException("Argument of unary operator - must be a number");
    }
  }


  @Override
  public int getPriority() {

    return 15;
  }


  @Override
  public void write(PrintWriter w) {

    w.print('-');
    this.e.write(w, this.e.getPriority() <= this.getPriority());
  }
}