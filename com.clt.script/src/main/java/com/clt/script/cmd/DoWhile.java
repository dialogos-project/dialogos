/*
 * @(#)DoWhile.java
 * Created on Wed Nov 27 2002
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
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.expressions.Assignment;
import com.clt.script.exp.expressions.Constant;
import com.clt.script.exp.values.BoolValue;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DoWhile
    implements Command {

  Expression condition;
  Command body;


  public DoWhile(Expression condition, Command body) {

    if (body == null) {
      throw new IllegalArgumentException("null body for DoWhile");
    }
    this.condition = condition;
    this.body = body;

    if (this.condition == null) {
      this.condition = new Constant(true);
    }
  }


  public void execute(Debugger dbg) {

    try {
      do {
        dbg.preExecute(this);
        this.body.execute(dbg);
      } while (this.condition.evaluate(dbg).equals(BoolValue.TRUE));
    } catch (BreakMessage m) {
    }
  }


  public ReturnInfo check(Collection<String> warnings) {

    Type t = this.condition.getType();
    try {
      Type.unify(t, Type.Bool);
    } catch (TypeException exn) {
      throw new TypeException("condition <" + this.condition
                    + "> of \"do while\" loop is not a boolean expression:\n"
        + this.condition);
    }

    if ((warnings != null) && (this.condition instanceof Assignment)) {
      warnings.add("Possibly unwanted assignment: do ... while ("
        + this.condition + ")");
    }

    // the body is executed at least once, so it determines the return status in
    // all cases
    ReturnInfo info = this.body.check(warnings);
    info.breakInfo = ReturnInfo.ON_NO_PATH;
    return info;
  }


  @Override
  public String toString() {

    return "do { ... } while " + this.condition;
  }
}
