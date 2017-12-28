//
//  MacError.java
//  AppleScript
//
//  Created by Daniel Bobbert on Thu Mar 18 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.mac;

public class MacError
    extends Exception {

  public MacError() {

    super();
  }


  public MacError(String message) {

    super(message);
  }


  public MacError(int error) {

    super("MacOS Error " + error + ": " + MacError.getErrorMessage(error));
  }


  private static String getErrorMessage(int error) {

    switch (error) {

      default:
        return "Unknown error";
    }
  }
}
