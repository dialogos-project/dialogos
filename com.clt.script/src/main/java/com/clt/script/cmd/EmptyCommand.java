/*
 * @(#)EmptyCommand.java
 * Created on Thu Nov 28 2002
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

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class EmptyCommand
    implements Command {

  public void execute(Debugger dbg) {

    dbg.preExecute(this);
  }


  public ReturnInfo check(Collection<String> warnings) {

    return new ReturnInfo(ReturnInfo.ON_NO_PATH, null);
  }
}
