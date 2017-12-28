//
//  Variable.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import com.clt.script.exp.Type;

public class VarDef {

  public String name;
  public Type type;


  public VarDef(String name, Type type) {

    this.name = name;
    this.type = type;
  }
}