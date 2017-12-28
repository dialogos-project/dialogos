/*
 * @(#)StructExpression.java
 * Created on Fri Oct 25 2002
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.StructType;
import com.clt.script.exp.values.StructValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class StructExpression extends Expression {

  private Map<String, Expression> slots = new HashMap<String, Expression>();


  public StructExpression() {

    this(new String[0], new Expression[0]);
  }


  public StructExpression(String labels[], Expression expressions[]) {

    if (labels.length != expressions.length) {
      throw new IllegalArgumentException(
        "Number of labels does not match number of struct elements");
    }

    for (int i = 0; i < labels.length; i++) {
      this.add(labels[i], expressions[i]);
    }
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    String labels[] = new String[this.slots.size()];
    Expression values[] = new Expression[labels.length];
    int n = 0;
    for (Iterator<String> it = this.labels(); it.hasNext(); n++) {
      labels[n] = it.next();
      values[n] = this.getExpression(labels[n]).copy(mapping);
    }

    try {
      return this.getClass().getConstructor(
        new Class[] { labels.getClass(), values.getClass() }).
        newInstance(new Object[] { labels, values });
    } catch (Exception exn) {
      try {
        StructExpression s = this.getClass().newInstance();
        for (int i = 0; i < labels.length; i++) {
          s.add(labels[i], values[i]);
        }
        return s;
      } catch (Exception exn2) {
        throw new AbstractMethodError(this.getClass().getName()
          + " does not override copy()");
      }
    }
  }


  protected void add(String label, Expression expression) {

    this.slots.put(label, expression);
  }


  @Override
  protected Value eval(Debugger dbg) {

    String[] labels = new String[this.slots.size()];
    Value[] values = new Value[this.slots.size()];
    int i = 0;
    for (Iterator<String> it = this.labels(); it.hasNext(); i++) {
      labels[i] = it.next();
      values[i] = this.getExpression(labels[i]).evaluate(dbg);
    }

    return new StructValue(labels, values);
  }


  public Iterator<String> labels() {

    return this.slots.keySet().iterator();
  }


  public Expression getExpression(String label) {

    Expression e = this.slots.get(label);
    if (e == null) {
      throw new EvaluationException("Structure does not contain element "
        + label);
    }
    else {
      return e;
    }
  }


  @Override
  public Type getType() {

    String labels[] = new String[this.slots.size()];
    Type types[] = new Type[this.slots.size()];
    int i = 0;
    for (Iterator<String> it = this.labels(); it.hasNext(); i++) {
      labels[i] = it.next();
      types[i] = this.getExpression(labels[i]).getType();
    }
    return new StructType(labels, types, false);
  }


  @Override
  public int getPriority() {

    return Integer.MAX_VALUE;
  }


  @Override
  public void write(PrintWriter w) {

    Set<String> labels = new TreeSet<String>(this.slots.keySet());

    w.print("{ ");
    for (Iterator<String> it = labels.iterator(); it.hasNext();) {
      String label = it.next();
      Expression value = this.getExpression(label);
      w.print(label);
      w.print(" = ");
      value.write(w);
      if (it.hasNext()) {
        w.print(", ");
      }
    }
    w.print(" }");
  }
}
