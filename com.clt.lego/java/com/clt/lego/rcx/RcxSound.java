/*
 * @(#)RcxSound.java
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

/**
 * @author dabo
 * 
 */
public enum RcxSound {
    Click(0),
    Beep(1),
    DownwardSweep(2),
    UpwardSweep(3),
    Error(4),
    FastUpwardSweep(5);

  private int value;


  private RcxSound(int value) {

    this.value = value;
  }


  public int getValue() {

    return this.value;
  }
}
