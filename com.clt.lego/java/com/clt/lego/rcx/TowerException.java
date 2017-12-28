/*
 * @(#)TowerException.java
 * Created on 04.06.2007 by dabo
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

package com.clt.lego.rcx;

import com.clt.lego.BrickException;
import com.clt.resources.DynamicResourceBundle;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class TowerException
    extends BrickException {

  public static final int RCX_OK = 0;
  public static final int RCX_NO_TOWER = -1;
  public static final int RCX_BAD_LINK = -2;
  public static final int RCX_BAD_ECHO = -3;
  public static final int RCX_NO_RESPONSE = -4;
  public static final int RCX_BAD_RESPONSE = -5;
  public static final int RCX_WRITE_FAIL = -6;
  public static final int RCX_READ_FAIL = -7;
  public static final int RCX_OPEN_FAIL = -8;
  public static final int RCX_INTERNAL_ERR = -9;
  public static final int RCX_ALREADY_CLOSED = -10;
  public static final int RCX_ALREADY_OPEN = -11;
  public static final int RCX_NOT_OPEN = -12;
  public static final int RCX_TIMED_OUT = -13;

  public static final int RCX_NOT_IMPL = -256;

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        TowerException.class.getPackage().getName() + ".Tower", null,
        TowerException.class.getClassLoader());


  public TowerException(int error) {

    this(TowerException.getErrorMessage(error));
  }


  public TowerException(String message) {

    super(message);
  }


  public TowerException(String message, Throwable cause) {

    super(message, cause);
  }


  protected static String getErrorMessage(int error) {

    String key =
      "0x" + StringTools.toHexString(0x10000 + error, 4).toUpperCase();
    String msg = TowerException.resources.getString(key);
    if ((msg != null) && !msg.equals(key)) {
      return msg;
    }
    else {
      return "Unknown tower status " + error;
    }
  }
}
