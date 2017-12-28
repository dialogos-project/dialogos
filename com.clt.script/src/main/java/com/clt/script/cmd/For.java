//
//  For.java
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
import com.clt.script.exp.expressions.Constant;
import com.clt.script.exp.values.BoolValue;

public class For
    implements Loop {

  private Command init;
  private Expression condition;
  private Command step;
  private Command body;


  public For(Command init, Expression condition, Command step) {

    this.init = init;
    this.condition = condition;
    this.step = step;
    this.body = new EmptyCommand();

    if (this.condition == null) {
      this.condition = new Constant(true);
    }
  }


  public void setBody(Command body) {

    this.body = body;
  }


  public void execute(Debugger dbg) {

    if (this.init != null) {
      this.init.execute(dbg);
    }

    try {
      while (this.condition.evaluate(dbg).equals(BoolValue.TRUE)) {
        if (this.body != null) {
          this.body.execute(dbg);
        }
        if (this.step != null) {
          this.step.execute(dbg);
        }
      }
    } catch (BreakMessage m) {
    }
  }


  public ReturnInfo check(Collection<String> warnings) {

    ReturnInfo info = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

    if (this.init != null) {
      info = this.init.check(warnings);
      if (info.info == ReturnInfo.ON_ALL_PATHS) {
        throw new ExecutionException("Body of \"for\" loop is never reached.");
      }
    }

    Type t = this.condition.getType();
    try {
      Type.unify(t, Type.Bool);
    } catch (TypeException exn) {
      throw new TypeException("condition <" + this.condition
                    + "> of \"for\" loop is not a boolean expression");
    }

    ReturnInfo loopInfo = new ReturnInfo(ReturnInfo.ON_NO_PATH, null);

    ReturnInfo bodyInfo = this.body.check(warnings);
    if (this.step != null) {
      bodyInfo = bodyInfo.append(this.step.check(warnings));
    }

    loopInfo = loopInfo.merge(bodyInfo);

    info = info.append(loopInfo);

    info.breakInfo = ReturnInfo.ON_NO_PATH;

    return info;
  }


  @Override
  public String toString() {

    return "for (" + this.init + "; " + this.condition + "; " + this.step + ")";
  }
}
