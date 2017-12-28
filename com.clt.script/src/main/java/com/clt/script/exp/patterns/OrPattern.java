/*
 * @(#)OrPattern.java
 * Created on Mon Oct 20 2003
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

package com.clt.script.exp.patterns;

import java.util.Map;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.TypeVariable;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class OrPattern
    implements Pattern {

  Pattern[] patterns;


  public OrPattern(Pattern[] patterns) {

    if ((patterns == null) || (patterns.length == 0)) {
      throw new IllegalArgumentException("Empty pattern array");
    }
    this.patterns = new Pattern[patterns.length];
    // copy pattern array. Also make sure that they all match the same variables
    VarSet vars = null;
    for (int i = 0; i < patterns.length; i++) {
      this.patterns[i] = patterns[i];
      if (vars == null) {
        vars = patterns[i].getFreeVars();
      }
      else {
        if (!vars.equals(patterns[i].getFreeVars())) {
          throw new IllegalArgumentException(
                      "All alternatives of an OR-pattern must bind the same variables");
        }
      }
    }
  }


  public Match match(Value v) {

    for (int i = 0; i < this.patterns.length; i++) {
      Match m = this.patterns[i].match(v);
      if (m != null) {
        return m;
      }
    }
    return null;
  }


  public Type getType(Map<String, Type> variableTypes) {

    Type result = this.patterns[0].getType(variableTypes);
    for (int i = 1; i < this.patterns.length; i++) {
      Type t = this.patterns[i].getType(variableTypes);
      if (result != null) {
        try {
          result = Type.unify(result, t);
        } catch (Exception exn) {
          result = null;
        }
      }
    }
    return result != null ? result : new TypeVariable();
  }


  public Pattern.VarSet getFreeVars() {

    return this.patterns[0].getFreeVars();
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();
    for (int i = 0; i < this.patterns.length; i++) {
      if (i > 0) {
        b.append(" | ");
      }
      b.append('(');
      b.append(this.patterns[i].toString());
      b.append(')');
    }
    return b.toString();
  }
}
