/*
 * @(#)Constant.java
 * Created on Wed Oct 16 2002
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
import com.clt.script.exp.Expression;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;
import com.clt.script.exp.values.BoolValue;
import com.clt.script.exp.values.IntValue;
import com.clt.script.exp.values.RealValue;
import com.clt.script.exp.values.StringValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class Constant extends Expression implements Pattern {
  Value v;
  Type t;


  protected Constant(Value v, Type t) {

    this.v = v;
    this.t = t;
  }


  public Constant(String value) {

    this.v = new StringValue(value);
    this.t = Type.String;
  }


  /*
   * public Constant(Time value) { o = value; t = Time.TYPE; }
   */
  public Constant(long value) {

    this.v = new IntValue(value);
    this.t = Type.Int;
  }


  public Constant(double value) {

    this.v = new RealValue(value);
    this.t = Type.Real;
  }


  public Constant(boolean value) {

    this.v = new BoolValue(value);
    this.t = Type.Bool;
  }


  @Override
  protected Value eval(Debugger dbg) {

    return this.v;
  }


  @Override
  public Type getType() {

    return this.t;
  }


  public Type getType(Map<String, Type> variableTypes) {

    return this.getType();
  }


  public Match match(Value v) {
    return this.v.equals(v) ? new Match() : null;
  }


  public Pattern.VarSet getFreeVars() {

    return new Pattern.VarSet();
  }


  @Override
  public int getPriority() {

    return Integer.MAX_VALUE;
  }


  @Override
  public Expression copy(Map<?, ?> mapping) {

    return new Constant(this.v.copy(), this.t.copy());
  }


  @Override
  public void write(PrintWriter w) {

    if (this.v instanceof StringValue) {
      w.print(StringValue.toSourceString(((StringValue)this.v).getString(),
        true));
    }
    else {
      w.print(this.v.toString());
    }
  }


  public static Constant negate(Constant c) {

    if (c.v instanceof IntValue) {
      return new Constant(-((IntValue)c.v).getInt());
    }
    else if (c.v instanceof RealValue) {
      return new Constant(-((RealValue)c.v).getReal());
    }
    else {
      throw new IllegalArgumentException(
        "Cannot negate constant that is not a number");
    }
  }

  public static class Undefined
        extends Constant {

    public Undefined() {

      super(new com.clt.script.exp.values.Undefined(), new TypeVariable());
    }
  }
}