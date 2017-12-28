/*
 * @(#)StructPattern.java
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

package com.clt.script.exp.patterns;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.clt.script.exp.EvaluationException;
import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.Value;
import com.clt.script.exp.types.StructType;
import com.clt.script.exp.values.StructValue;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class StructPattern
    implements Pattern {

  private Map<String, Pattern> slots = new HashMap<String, Pattern>();


  public StructPattern() {

    this(new String[0], new Pattern[0]);
  }


  public StructPattern(String labels[], Pattern patterns[]) {

    if (labels.length != patterns.length) {
      throw new IllegalArgumentException(
              "Number of labels does not match number of element patterns");
    }

    for (int i = 0; i < labels.length; i++) {
      this.add(labels[i], patterns[i]);
    }
  }


  protected void add(String label, Pattern pattern) {

    this.slots.put(label, pattern);
  }


  public Iterator<String> labels() {

    return this.slots.keySet().iterator();
  }


  public Pattern getPattern(String label) {

    Pattern p = this.slots.get(label);
    if (p == null) {
      throw new EvaluationException(
        "Structure pattern does not contain element " + label);
    }
    else {
      return p;
    }
  }


  public Type getType(Map<String, Type> variableTypes) {

    String labels[] = new String[this.slots.size()];
    Type types[] = new Type[this.slots.size()];
    int i = 0;
    for (Iterator<String> it = this.labels(); it.hasNext(); i++) {
      labels[i] = it.next();
      types[i] = this.getPattern(labels[i]).getType(variableTypes);
    }
    return new StructType(labels, types, true);
  }


  public Pattern.VarSet getFreeVars() {

    Pattern.VarSet s = new Pattern.VarSet();
    for (Iterator<String> it = this.labels(); it.hasNext();) {
      String label = it.next();
      s.addAll(this.getPattern(label).getFreeVars());
    }
    return s;
  }


  public Match match(Value v) {

    if (v instanceof StructValue) {
      Match match = new Match();
      for (Iterator<String> it = this.labels(); it.hasNext();) {
        String label = it.next();
        Value elem = null;
        try {
          elem = ((StructValue)v).getValue(label);
        } catch (Exception exn) {
          return null;
        }

        Match m = this.getPattern(label).match(elem);
        if (m == null) {
          return null;
        }
        else {
          match.merge(m);
        }
      }
      return match;
    }
    else {
      return null;
    }
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();
    b.append("{ ");
    for (Iterator<String> it = this.labels(); it.hasNext();) {
      String label = it.next();
      b.append(label);
      b.append(" = ");
      b.append(this.getPattern(label).toString());

      if (it.hasNext()) {
        b.append(", ");
      }
    }
    b.append(" }");
    return b.toString();
  }
}
