/*
 * @(#)Not.java
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
import com.clt.script.exp.Value;
import com.clt.script.exp.values.BoolValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class Not extends Expression {

  Expression e;


  public Not(Expression e) {

    this.e = e;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Not(this.e.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v = this.e.evaluate(dbg);

    if (v instanceof BoolValue) {
      return (new BoolValue(!((BoolValue)v).getBool()));
    }
    else {
      throw new EvaluationException("Illegal arguments: can't evaluate !(" + v
        + ")");
    }
  }


  @Override
  public Type getType() {

    return Type.unify(this.e.getType(), Type.Bool);
  }


  @Override
  public int getPriority() {

    return 14;
  }


  @Override
  public void write(PrintWriter w) {

    w.print('!');
    this.e.write(w, this.e.getPriority() <= this.getPriority());
  }
}