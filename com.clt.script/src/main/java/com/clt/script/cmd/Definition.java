//
//  Definition.java
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

/**
 * Definitions of the form <code><i>type</i> <i>id</i>;</code> and
 * <code><i>type</i> <i>id</i> = <i>exp</i>;</code>
 */

public class Definition
    implements Command {

  private String name;
  private Type type;
  private Expression exp;
  private Block block;


  public Definition(String name, Type type, Expression exp, Block currentBlock) {

    this.name = name;
    this.type = type;
    this.exp = exp;

    this.block = currentBlock;

    this.block.addVariable(name, type);
  }


  public String getName() {

    return this.name;
  }


  public Type getType() {

    return this.type;
  }


  public Block getBlock() {

    return this.block;
  }


  public boolean hasInitValue() {

    return this.exp != null;
  }


  public void execute(Debugger dbg) {

    dbg.preExecute(this);

    if (this.exp != null) {
      this.block.setVariableValue(this.name, this.exp.evaluate(dbg));
    }
  }


  public ReturnInfo check(Collection<String> warnings) {

    try {
      if (this.exp != null) {
        Type.unify(this.exp.getType(), this.type);
      }
    } catch (TypeException exn) {
      throw new TypeException("Incompatible types in definition of variable "
        + this.name + ": "
                    + exn.getLocalizedMessage());
    }
    return new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
  }


  @Override
  public String toString() {

    return this.type + " " + this.name
      + (this.exp != null ? " = " + this.exp : "");
  }
}
