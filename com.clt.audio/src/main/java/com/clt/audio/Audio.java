//
//  Audio.java
//  Wizard
//
//  Created by Daniel Bobbert on Thu Jun 20 2002.
//  Copyright (c) 2002 CLT Sprachtechnologie GmbH. All rights reserved.
//

package com.clt.audio;

import com.clt.resources.DynamicResourceBundle;

class Audio {

  private static DynamicResourceBundle resources =
    new DynamicResourceBundle("com.clt.audio.Resources");


  static String getString(String key) {

    return Audio.resources.getString(key);
  }

}
