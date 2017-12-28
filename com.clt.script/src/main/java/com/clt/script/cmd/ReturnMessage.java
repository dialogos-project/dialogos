//
//  ReturnMessage.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import com.clt.script.exp.Value;

public class ReturnMessage extends RuntimeException {

  Value returnValue;


  public ReturnMessage(Value returnValue) {

    this.returnValue = returnValue;
  }


  public Value getReturnValue() {

    return this.returnValue;
  }
}
