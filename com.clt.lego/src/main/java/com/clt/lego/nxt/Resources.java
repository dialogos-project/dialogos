/*
 * @(#)Resources.java
 * Created on 10.07.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.lego.nxt;

import com.clt.resources.DynamicResourceBundle;

/**
 * @author dabo
 * 
 */
class Resources {

  private static DynamicResourceBundle resources =
    new DynamicResourceBundle(
        Resources.class.getPackage().getName() + ".NXT", null, Resources.class
          .getClassLoader());


  /**
   * Return a localized version of the given string.
   */
  public static String getString(String key) {

    return Resources.resources.getString(key);
  }
}
