//
//  ExpCmd.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;

public class ExpCmd
    implements Command {

  Expression e;


  public ExpCmd(Expression e) {

    this.e = e;
  }


  public void execute(Debugger dbg) {

    dbg.preExecute(this);
    this.e.evaluate(dbg);
  }


  public ReturnInfo check(Collection<String> warnings) {

    this.e.getType();

    return new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
  }


  @Override
  public String toString() {

    return this.e.toString();
  }
}
