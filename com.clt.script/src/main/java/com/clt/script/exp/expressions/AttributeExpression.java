/*
 * @(#)AttributeExpression.java
 * Created on Mon Oct 06 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
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
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.Undefined;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class AttributeExpression extends Expression {

  private Expression e;
  private String attribute;


  public AttributeExpression(Expression e, String attribute) {

    this.e = e;
    this.attribute = attribute;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new AttributeExpression(this.e.copy(mapping), this.attribute);
  }


  @Override
  protected Value eval(Debugger dbg) {

    Value v = this.e.evaluate(dbg);
    Value att = v.getAttribute(this.attribute);
    if (att == null) {
      return new Undefined();
    }
    else {
      return att;
    }
  }


  @Override
  public Type getType() {

    return new TypeVariable();
  }


  @Override
  public int getPriority() {

    return 17;
  }


  @Override
  public void write(PrintWriter w) {

    this.e.write(w, this.e.getPriority() < this.getPriority());
    w.print('#');
    w.print(this.attribute);
  }
}
