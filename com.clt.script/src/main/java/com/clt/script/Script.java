/*
 * @(#)Script.java
 * Created on Mon Oct 06 2003
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

package com.clt.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clt.script.cmd.Block;
import com.clt.script.cmd.BreakMessage;
import com.clt.script.cmd.Prototype;
import com.clt.script.cmd.ReturnInfo;
import com.clt.script.cmd.ReturnMessage;
import com.clt.script.debug.Debugger;
import com.clt.script.exp.Expression;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.parser.Parser;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Script {

  private List<Prototype> procedures;
  private Map<String, Type> types;

  private Block main;


  public Script() {

    this.procedures = new ArrayList<Prototype>();
    this.types = new HashMap<String, Type>();

    this.main = new Block(null);
  }


  public void add(Prototype procedure) {

    if (this.getProcedure(procedure.getName(),
      procedure.getParameterTypes().length) != null) {
      throw new TypeException("Duplicate definition of function "
        + procedure.getName()
                  + "()");
    }

    this.procedures.add(procedure);
  }


  public void add(String name, Type type) {

    if (this.types.containsKey(name)) {
      throw new TypeException("Duplicate definition of type " + name);
    }
    this.types.put(name, type);
  }


  public Block getMain() {

    return this.main;
  }


  public Type getType(String typename) {

    return this.types.get(typename);
  }


  public Prototype getProcedure(String name, int numArgs) {

    for (Prototype p : this.procedures) {
      if (name.equals(p.getName()) && (p.getParameterTypes().length == numArgs)) {
        return p;
      }
    }
    return null;
  }


  public void execute(Debugger dbg) {

    try {
      this.main.execute(dbg);
    } catch (ReturnMessage msg) {
    } catch (BreakMessage msg) {
    }
  }


  public void check(Collection<String> warnings) {

    for (Prototype p : this.procedures) {
      p.check(warnings);
    }

    ReturnInfo info = this.main.check(warnings);
    if (info.type != null ? !Type.equals(info.type, Type.Void) : false) {
      throw new TypeException("Illegal return statement in main block");
    }
  }


  /*****************************************************************/

  public static Script parseScript(String script)
      throws Exception {

    return Script.parseScript(script, new DefaultEnvironment());
  }


  public static Script parseScript(String script, Environment env)
      throws Exception {

    return Parser.parseScript(script, env);
  }


  public static Script parseFunctions(String fundefs)
      throws Exception {

    return Script.parseFunctions(fundefs, new DefaultEnvironment());
  }


  public static Script parseFunctions(String fundefs, Environment env)
      throws Exception {

    return Parser.parseFunctions(fundefs, env);
  }


  public static Expression parseExpression(String exp)
      throws Exception {

    return Script.parseExpression(exp, new DefaultEnvironment());
  }


  public static Expression parseExpression(String exp, Environment env)
      throws Exception {

    return Parser.parseExpression(exp, env);
  }


  public static Pattern parsePattern(String exp)
      throws Exception {

    return Parser.parsePattern(exp);
  }
}
