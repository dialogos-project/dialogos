/*
 * @(#)Display.java
 * Created on 29.06.2007 by dabo
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
public enum DisplayMode {
    CLOCK(0),
    SENSOR_1(1),
    SENSOR_2(2),
    SENSOR_3(3),
    MOTOR_A(4),
    MOTOR_B(5),
    MOTOR_C(6);

  private int value;


  private DisplayMode(int value) {

    this.value = value;
  }


  public int getValue() {

    return this.value;
  }
}
