/*
 * @(#)Match.java
 * Created on Fri Oct 17 2003
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

package com.clt.script.exp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Match {

  Map<String, Value> match;


  public Match() {

    this.match = new HashMap<String, Value>();
  }


  public void put(String variable, Value value) {

    this.match.put(variable, value);
  }


  public Value get(String variable) {

    return this.match.get(variable);
  }


  public void merge(Match m) {

    this.match.putAll(m.match);
  }


  public Iterator<String> variables() {

    return this.match.keySet().iterator();
  }
}
