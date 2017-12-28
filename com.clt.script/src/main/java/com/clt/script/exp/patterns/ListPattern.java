/*
 * @(#)ListPattern.java
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.clt.script.exp.Match;
import com.clt.script.exp.Pattern;
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

public class ListPattern
    implements Pattern {

  Pattern[] patterns;


  public ListPattern(Pattern[] patterns) {

    this(Arrays.asList(patterns));
  }


  public ListPattern(List<Pattern> patterns) {

    this.patterns = patterns.toArray(new Pattern[patterns.size()]);
  }


  public Type getType(Map<String, Type> variableTypes) {

    if (this.patterns.length == 0) {
      return new ListType();
    }
    else {
      return new ListType(this.patterns[0].getType(variableTypes));
    }
  }


  public Pattern.VarSet getFreeVars() {

    Pattern.VarSet s = new Pattern.VarSet();
    for (int i = 0; i < this.patterns.length; i++) {
      s.addAll(this.patterns[i].getFreeVars());
    }
    return s;
  }


  public Match match(Value v) {

    if (v instanceof ListValue) {
      Match match = new Match();
      if (((ListValue)v).size() != this.patterns.length) {
        return null;
      }

      Iterator<Value> it = ((ListValue)v).iterator();
      for (int i = 0; i < this.patterns.length; i++) {
        Match m = this.patterns[i].match(it.next());
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
    b.append("[");
    for (int i = 0; i < this.patterns.length; i++) {
      if (i > 0) {
        b.append(", ");
      }
      b.append(this.patterns[i].toString());
    }
    b.append("]");
    return b.toString();
  }
}
