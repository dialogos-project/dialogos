/*
 * @(#)Cons.java
 * Created on Tue Aug 03 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script.exp.expressions;

import java.util.Map;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.ListType;
import com.clt.script.exp.values.ListValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Cons extends BinaryOperator {

  public Cons(Expression e1, Expression e2) {

    super("::", BinaryOperator.RIGHT, e1, e2);
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Cons(this.e1.copy(mapping), this.e2.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v1 = this.e1.evaluate(dbg);
    Value v2 = this.e2.evaluate(dbg);

    if (v2 instanceof ListValue) {
      ListValue l = (ListValue)v2;
      Value[] l2 = new Value[l.size() + 1];
      l2[0] = v1;
      for (int i = 0; i < l.size(); i++) {
        l2[i + 1] = l.get(i);
      }
      return new ListValue(l2);
    }
    else {
      throw new EvaluationException("Illegal arguments: can't evaluate " + v1
        + " :: " + v2);
    }
  }


  @Override
  public Type getType() {

    Type t1 = this.e1.getType().resolve();
    Type t2 = this.e2.getType().resolve();

    return Type.unify(new ListType(t1), t2);
  }


  @Override
  public int getPriority() {

    return 12;
  }
}
