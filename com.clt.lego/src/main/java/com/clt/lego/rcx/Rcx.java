/*
 * @(#)Rcx.java
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

package com.clt.lego.rcx;

import java.io.IOException;

import com.clt.lego.Brick;

/**
 * @author dabo
 * 
 */
public interface Rcx
    extends Brick {

  public void playSound(RcxSound sound)
      throws IOException;


  public void setProgramNumber(int program)
      throws IOException;


  public int getCurrentProgram()
      throws IOException;


  public int[] getMemoryMap()
      throws IOException;


  public void setDisplayMode(DisplayMode display)
      throws IOException;


  public void showNumber(int number)
      throws IOException;


  public void setTime(byte hours, byte minutes)
      throws IOException;


  public void setSensorType(int sensor, Sensor.Type type, Sensor.Mode mode,
      int slope)
        throws IOException;


  public int getSensorValue(int sensor)
      throws IOException;


  public int getSensorRawValue(int sensor)
      throws IOException;


  public void setMotorPower(int value, Motor.Port... motors)
      throws IOException;


  public void setMotorDirection(Motor.Direction direction, Motor.Port... motors)
        throws IOException;


  public void setMotorState(Motor.State state, Motor.Port... motors)
      throws IOException;


  public int getVariable(int variable)
      throws IOException;


  public void setVariable(int variable, int value)
      throws IOException;


  public void uploadFirmware(byte[] data)
      throws IOException;
}
