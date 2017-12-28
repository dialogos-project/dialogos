//
//  If.java
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
import com.clt.script.exp.values.BoolValue;

/**
 * Commands of the form <code>if (<i>exp</i>) <i>cmd</i></code> and
 * <code>if (<i>exp</i>) <i>cmd</i> else <i>cmd</i></code>
 */

public class If
    implements Command {

  Expression condition;
  Command consequence;
  Command alternative;


  public If(Expression condition, Command consequence) {

    this(condition, consequence, null);
  }


  public If(Expression condition, Command consequence, Command alternative) {

    if (condition == null) {
      throw new IllegalArgumentException("condition must not be null");
    }
    if (consequence == null) {
      throw new IllegalArgumentException("consequence must not be null");
    }

    this.condition = condition;
    this.consequence = consequence;
    this.alternative = alternative;
  }


  public void execute(Debugger dbg) {

    dbg.preExecute(this);
    if (this.condition.evaluate(dbg).equals(BoolValue.TRUE)) {
      this.consequence.execute(dbg);
    }
    else if (this.alternative != null) {
      this.alternative.execute(dbg);
    }
  }


  public ReturnInfo check(Collection<String> warnings) {

    Type t = this.condition.getType();
    try {
      Type.unify(t, Type.Bool);
    } catch (TypeException exn) {
      throw new TypeException("condition <" + this.condition
        + "> is not a boolean expression");
    }

    if ((warnings != null) && (this.condition instanceof Assignment)) {
      warnings.add("Possibly unwanted assignment: if (" + this.condition + ")");
    }

    ReturnInfo info = this.consequence.check(warnings);
    if (this.alternative != null) {
      info.merge(this.alternative.check(warnings));
    }
    else {
      info.merge(new ReturnInfo(ReturnInfo.ON_NO_PATH, null));
    }
    return info;
  }


  @Override
  public String toString() {

    return "if " + this.condition;
  }
}
