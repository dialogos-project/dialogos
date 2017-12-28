/*
 * @(#)ScriptEnvironment.java
 * Created on 05.02.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.script;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import com.clt.script.cmd.Prototype;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Value;
import com.clt.script.exp.Variable;
import com.clt.script.exp.expressions.Function;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ScriptEnvironment implements Environment {

  Script script;


  public ScriptEnvironment(Script script) {

    this.script = script;
  }


  public Type getType(String typeName) {

    Type t = this.script.getType(typeName);
    if (t != null) {
      return t;
    }
    else {
      throw new TypeException("Unknown type: " + typeName);
    }
  }


  public Variable createVariableReference(String name) {

    throw new TypeException("Unknown variable: " + name);
  }


  public Expression createFunctionCall(String name, final Expression[] arguments) {

    final Prototype proc = this.script.getProcedure(name, arguments.length);
    if (proc != null) {
      return new Function(proc.getName(), arguments) {

        @Override
        protected Value eval(Debugger dbg, Value[] args) {

          return proc.getProcedure().call(dbg, args);
        }


        @Override
        public Type getType() {

          return proc.getReturnType();
        }
      };
    }
    else {
      throw new Environment.NoSuchFunctionException(name, arguments);
    }
  }


  public Reader include(String name)
      throws IOException {

    throw new FileNotFoundException(name);
  }
}
