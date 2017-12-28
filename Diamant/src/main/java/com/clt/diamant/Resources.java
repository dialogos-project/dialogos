//
//  Resources.java
//  Wizard
//
//  Created by Daniel Bobbert on Tue Jun 25 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.diamant;

import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

public class Resources {

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        "com.clt.diamant.DiaMant");


  public static String getString(String key) {

    return Resources.resources.getString(key);
  }


  public static String format(String key, Object... param) {

    return StringTools.format(Resources.getString(key), param);
  }


  public static DynamicResourceBundle getResources() {

    return Resources.resources;
  }
}
