/*
 * @(#)Or.java
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

public class Or extends BinaryOperator {

  public Or(Expression e1, Expression e2) {

    super("||", BinaryOperator.LEFT, e1, e2);
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Or(this.e1.copy(mapping), this.e2.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v1 = this.e1.evaluate(dbg);

    if (v1 instanceof BoolValue) {
      if (!((BoolValue)v1).getBool()) {
        Value v2 = this.e2.evaluate(dbg);
        if (v2 instanceof BoolValue) {
          return new BoolValue(((BoolValue)v2).getBool());
        }
        else {
          throw new EvaluationException("Illegal arguments to operator ||: "
            + v2);
        }
      }
      else {
        return BoolValue.TRUE;
      }
    }
    else {
      throw new EvaluationException("Illegal arguments to operator ||: " + v1);
    }
  }


  @Override
  public Type getType() {

    Type.unify(this.e1.getType(), Type.Bool);
    Type.unify(this.e2.getType(), Type.Bool);

    return Type.Bool;
  }


  @Override
  public int getPriority() {

    return 3;
  }
}