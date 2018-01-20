//
//  AppleEventHandler.java
//  AppleScript
//
//  Created by Daniel Bobbert on Thu Mar 18 2004.
//  Copyright (c) 2004 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.mac;

import java.util.Hashtable;

interface AppleEventHandler {
  @SuppressWarnings("unchecked")
  public Object handleEvent(Object directObject, Hashtable parameters);
}
