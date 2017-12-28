/*
 * @(#)BinaryOperator.java
 * Created on 18.10.04
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

import java.io.PrintWriter;

import com.clt.script.exp.Expression;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

abstract class BinaryOperator
    extends Expression {

  protected static final int LEFT = 0;
  protected static final int RIGHT = 1;
  protected static final int NONE = 2;

  protected String op;
  protected int associativity;
  protected Expression e1, e2;


  public BinaryOperator(String op, int associativity, Expression e1,
      Expression e2) {

    this.op = op;
    this.associativity = associativity;
    this.e1 = e1;
    this.e2 = e2;
  }


  @Override
  public void write(PrintWriter w) {

    this.e1.write(w, this.associativity == BinaryOperator.LEFT ? this.e1
      .getPriority() < this.getPriority()
                : this.e1.getPriority() <= this.getPriority());
    w.print(' ');
    w.print(this.op);
    w.print(' ');
    this.e2.write(w, this.associativity == BinaryOperator.RIGHT ? this.e2
      .getPriority() < this.getPriority()
                : this.e2.getPriority() <= this.getPriority());
  }
}
