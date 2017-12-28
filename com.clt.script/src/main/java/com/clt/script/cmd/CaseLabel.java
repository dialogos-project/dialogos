/*
 * @(#)CaseLabel.java
 * Created on Thu Dec 12 2002
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

package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Value;
import com.clt.script.exp.expressions.Constant;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class CaseLabel implements Command {

  Constant c;


  public CaseLabel(Constant c) {

    this.c = c;
  }


  public void execute(Debugger dbg) {

  }


  public ReturnInfo check(Collection<String> warnings) {

    return new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
  }


  public Value evaluate() {

    return this.c == null ? null : this.c.evaluate();
  }


  @Override
  public String toString() {

    return this.c == null ? "default" : this.c.toString();
  }
}