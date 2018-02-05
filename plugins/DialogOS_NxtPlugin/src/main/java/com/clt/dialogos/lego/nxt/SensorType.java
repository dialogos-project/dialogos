/*
 * @(#)SensorType.java
 * Created on 11.07.2007 by dabo
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

package com.clt.dialogos.lego.nxt;

public enum SensorType {
    NONE,
    TOUCH,
    LIGHT,
    SOUND,
    ULTRASONIC;

  @Override
  public String toString() {

    return Resources.getString("SENSORTYPE_" + this.name());
  }
}