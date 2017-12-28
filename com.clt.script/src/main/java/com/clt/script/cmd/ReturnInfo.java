//
//  ReturnInfo.java
//  DialogManager
//
//  Created by Daniel Bobbert on Tue Jul 30 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.script.cmd;

import com.clt.script.exp.Type;
import com.clt.script.exp.TypeException;

public class ReturnInfo {

  public static final int
      ON_NO_PATH = 0,
      ON_SOME_PATHS = 1,
      ON_ALL_PATHS = 2;

  public int info;
  public int breakInfo;
  public Type type;


  public ReturnInfo(int info, Type type) {

    this.info = info;
    this.type = type;
    this.breakInfo = ReturnInfo.ON_NO_PATH;
  }


  public ReturnInfo merge(ReturnInfo ri) {

    if ((this.info != ReturnInfo.ON_NO_PATH)
      && (ri.info != ReturnInfo.ON_NO_PATH)) {
      try {
        this.type = Type.unify(this.type, ri.type);
      } catch (TypeException exn) {
        throw new TypeException("Conflicting return types:\nt1 = " + this.type
          + "\nt2 = " + ri.type);
      }
    }
    else if (this.info == ReturnInfo.ON_NO_PATH) {
      this.type = ri.type;
    }

    if ((this.info == ReturnInfo.ON_NO_PATH)
      && (ri.info == ReturnInfo.ON_NO_PATH)) {
      this.info = ReturnInfo.ON_NO_PATH;
    }
    else if ((this.info == ReturnInfo.ON_ALL_PATHS)
      && (ri.info == ReturnInfo.ON_ALL_PATHS)) {
      this.info = ReturnInfo.ON_ALL_PATHS;
    }
    else {
      this.info = ReturnInfo.ON_SOME_PATHS;
    }

    if ((this.breakInfo == ReturnInfo.ON_NO_PATH)
      && (ri.breakInfo == ReturnInfo.ON_NO_PATH)) {
      this.breakInfo = ReturnInfo.ON_NO_PATH;
    }
    else if ((this.breakInfo == ReturnInfo.ON_ALL_PATHS)
      && (ri.breakInfo == ReturnInfo.ON_ALL_PATHS)) {
      this.breakInfo = ReturnInfo.ON_ALL_PATHS;
    }
    else {
      this.breakInfo = ReturnInfo.ON_SOME_PATHS;
    }

    return this;
  }


  public ReturnInfo append(ReturnInfo ri) {

    if ((this.info != ReturnInfo.ON_NO_PATH)
      && (ri.info != ReturnInfo.ON_NO_PATH)) {
      try {
        this.type = Type.unify(this.type, ri.type);
      } catch (TypeException exn) {
        throw new TypeException("Conflicting return types:\nt1 = " + this.type
          + "\nt2 = " + ri.type);

      }
    }
    else if (this.info == ReturnInfo.ON_NO_PATH) {
      this.type = ri.type;
    }

    if ((this.info == ReturnInfo.ON_ALL_PATHS)
      || (ri.info == ReturnInfo.ON_ALL_PATHS)) {
      this.info = ReturnInfo.ON_ALL_PATHS;
    }
    else if ((this.info == ReturnInfo.ON_NO_PATH)
      && (ri.info == ReturnInfo.ON_NO_PATH)) {
      this.info = ReturnInfo.ON_NO_PATH;
    }
    else {
      this.info = ReturnInfo.ON_SOME_PATHS;
    }

    if ((this.breakInfo == ReturnInfo.ON_ALL_PATHS)
      || (ri.breakInfo == ReturnInfo.ON_ALL_PATHS)) {
      this.breakInfo = ReturnInfo.ON_ALL_PATHS;
    }
    else if ((this.breakInfo == ReturnInfo.ON_NO_PATH)
      && (ri.breakInfo == ReturnInfo.ON_NO_PATH)) {
      this.breakInfo = ReturnInfo.ON_NO_PATH;
    }
    else {
      this.breakInfo = ReturnInfo.ON_SOME_PATHS;
    }

    return this;
  }
}
