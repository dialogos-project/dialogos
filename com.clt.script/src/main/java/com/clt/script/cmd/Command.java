//
//  Command.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import java.util.Collection;

import com.clt.script.debug.Debugger;

public interface Command {

  public void execute(Debugger dbg);


  public ReturnInfo check(Collection<String> warnings);
}
