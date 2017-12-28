/*
 * @(#)Debugger.java
 * Created on Wed Nov 06 2002
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

package com.clt.script.debug;

import com.clt.script.cmd.Command;
import com.clt.script.exp.Expression;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface Debugger {

  public void preExecute(Command c);


  public void preEvaluate(Expression e);


  public void log(String s);
}
