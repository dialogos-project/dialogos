/*
 * @(#)RcxDeviceInfo.java
 * Created on 05.06.2007 by dabo
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

import com.clt.lego.BrickInfo;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public class RcxDeviceInfo
    extends BrickInfo {

  private int romVersion;


  public RcxDeviceInfo(String name, int firmwareVersion, int romVersion) {

    super(name, firmwareVersion);

    this.romVersion = romVersion;
  }


  public int getRomVersion() {

    return this.romVersion;
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();

    b.append("Name    : " + this.getName() + "\n");
    b.append("Firmware: 0x"
      + StringTools.toHexString(this.getFirmwareVersion(), 8) + "\n");
    b.append("ROM vers: 0x" + StringTools.toHexString(this.getRomVersion(), 8)
      + "\n");

    return b.toString();
  }

}
