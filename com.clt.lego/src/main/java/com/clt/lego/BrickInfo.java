/*
 * @(#)BrickInfo.java
 * Created on 26.04.2007 by dabo
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

package com.clt.lego;

import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class BrickInfo {

  private String name;
  private int firmwareVersion;


  public BrickInfo(String name, int firmwareVersion) {

    this.name = name;
    this.firmwareVersion = firmwareVersion;
  }


  public String getName() {

    return this.name;
  }


  public int getFirmwareVersion() {

    return this.firmwareVersion;
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();

    b.append("Name    : " + this.getName() + "\n");
    b.append("Firmware: 0x"
      + StringTools.toHexString(this.getFirmwareVersion(), 4) + "\n");

    return b.toString();
  }

}
