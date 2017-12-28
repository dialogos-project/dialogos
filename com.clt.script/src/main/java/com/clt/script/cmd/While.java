//
//  While.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.expressions.Assignment;
import com.clt.script.exp.expressions.Constant;
import com.clt.script.exp.values.BoolValue;

public class While
    implements Command {

  private Expression condition;
  private Command body;


  public While(Expression condition, Command body) {

    if (condition == null) {
      throw new IllegalArgumentException("null condition");
    }
    if (body == null) {
      throw new IllegalArgumentException("null body");
    }
    this.condition = condition;
    this.body = body;

    if (this.condition == null) {
      this.condition = new Constant(true);
    }
  }


  public void execute(Debugger dbg) {

    dbg.preExecute(this);
    try {
      while (this.condition.evaluate(dbg).equals(BoolValue.TRUE)) {
        this.body.execute(dbg);
        dbg.preExecute(this);
      }
    } catch (BreakMessage m) {
    }
  }


  public ReturnInfo check(Collection<String> warnings) {

    Type t = this.condition.getType();
    try {
      Type.unify(t, Type.Bool);
    } catch (TypeException exn) {
      throw new TypeException("condition <" + this.condition
                    + "> of \"while\" loop is not a boolean expression:\n"
        + this.condition);
    }

    if ((warnings != null) && (this.condition instanceof Assignment)) {
      warnings.add("Possibly unwanted assignment: while (" + this.condition
        + ") ... ");
    }

    // even if body always returns, the body may never be executed
    ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

    info = info.merge(this.body.check(warnings));

    info.breakInfo = ReturnInfo.ON_NO_PATH;

    return info;
  }


  @Override
  public String toString() {

    return "while " + this.condition + " { ... }";
  }
}
