/*
 * @(#)Nxt.java
 * Created on 12.04.2007 by dabo
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

import java.io.IOException;

import com.clt.lego.Brick;

/**
 * @author dabo
 * 
 */
public interface Nxt
    extends Brick {

  public static final String MODULE_EXTENSION = ".mod";
  public static final String PROGRAM_EXTENSION = ".rxe";
  public static final String SOUND_EXTENSION = ".rso";


  /** Return device info about this brick */
  public NxtDeviceInfo getDeviceInfo()
      throws IOException;


  /**
   * Play a tone with he given frequency in Hz for the given duration in ms. If
   * <code>wait</code> is true, this method will only return when the tone has
   * finished playing.
   */
  public void playTone(int frequency, int duration, boolean wait)
      throws IOException;


  public void playSoundFile(String filename, boolean loop)
      throws IOException;


  /** Stop sound playback */
  public void stopSoundPlayback()
      throws IOException;


  /** Get a list of executable programs on the brick */
  public String[] getPrograms()
      throws IOException;


  /** Get a list of modules on the brick */
  public String[] getModules()
      throws IOException;


  /** Return the ID of the given module */
  public int getModuleID(String name)
      throws IOException;


  /** Start executing the program specified by its name */
  public void startProgram(String filename)
      throws IOException;


  /**
   * Stop any running program on the brick. Return true if a program was stopped
   * or false if no program was runnning.
   **/
  public boolean stopProgram()
      throws IOException;


  /**
   * Return the name of the currently running program or <code>null</code> if no
   * program is running.
   */
  public String getCurrentProgram()
      throws IOException;


  public void setSensorType(int sensor, Sensor.Type type, Sensor.Mode mode,
      int slope)
        throws IOException;


  public Sensor.Type getSensorType(int sensor)
      throws IOException;


  public Sensor.Mode getSensorMode(int sensor)
      throws IOException;


  public int getSensorValue(int sensor)
      throws IOException;


  public int getSensorRawValue(int sensor)
      throws IOException;


  public void setOutputState(Motor.Port motor, int power, int mode,
      Motor.Regulation regulation,
            int turnRatio, Motor.State state, long tachoLimit)
      throws IOException;


  public MotorState getOutputState(Motor.Port motor)
      throws IOException;


  public void resetMotorPosition(Motor.Port motor, boolean relative)
      throws IOException;


  public byte[] lsRead(int sensor, byte[] data, int expectedResultSize)
      throws IOException;


  public byte[] readIOMap(String module, int offset, int length)
      throws IOException;


  public void writeIOMap(String module, int offset, byte[] data)
      throws IOException;


  public void sendMessage(int mailbox, String message)
      throws IOException;
}
