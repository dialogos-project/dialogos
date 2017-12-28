/*
 * @(#)Equal.java
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

public class Equal extends BinaryOperator {

  public Equal(Expression e1, Expression e2) {

    super("==", BinaryOperator.NONE, e1, e2);
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Equal(this.e1.copy(mapping), this.e2.copy(mapping));
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v1 = this.e1.evaluate(dbg);
    Value v2 = this.e2.evaluate(dbg);

    if ((v1 != null) && (v2 != null)) {
      return new BoolValue(v1.equals(v2));
    }
    else {
      throw new EvaluationException("Illegal arguments: can't evaluate " + v1
        + " == " + v2);
      /*
       * if (o1 instanceof IntValue && o2 instanceof IntValue) return (new
       * BoolValue(((IntValue) o1).getInt() == ((IntValue) o2).getInt())); else
       * if (o1 instanceof RealValue && o2 instanceof RealValue) return (new
       * BoolValue(((RealValue) o1).getReal() == ((RealValue) o2).getReal()));
       * else if (o1 instanceof BoolValue && o2 instanceof BoolValue) return
       * (new BoolValue(((BoolValue) o1).getBool() == ((BoolValue)
       * o2).getBool())); else if (o1 instanceof StringValue && o2 instanceof
       * StringValue) return (new BoolValue(((StringValue) o1).getString() ==
       * ((StringValue) o2).getString())); else throw new
       * EvaluationException("Illegal arguments: can't evaluate " + o1 + " == "
       * + o2);
       */
    }
  }


  @Override
  public Type getType() {

    Type.unify(this.e1.getType(), this.e2.getType());
    return Type.Bool;
  }


  @Override
  public int getPriority() {

    return 8;
  }
}