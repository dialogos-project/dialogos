/*
 * @(#)Environment.java
 * Created on Sun Oct 05 2003
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

import java.io.IOException;
import java.io.Reader;

import com.clt.script.exp.Expression;
import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;
import com.clt.script.exp.Variable;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Environment {

  public Type getType(String typeName);


  public Variable createVariableReference(String id);


  public Expression createFunctionCall(String name, Expression[] arguments);


  public Reader include(String name)
      throws IOException;

  public class NoSuchFunctionException extends TypeException {

    public NoSuchFunctionException(String name, Expression[] arguments) {

      super("Unknown function or wrong number/type of arguments: " +
        DefaultEnvironment.functionSignature(name, arguments));
    }


    public NoSuchFunctionException(String message) {

      super(message);
    }
  }
}
