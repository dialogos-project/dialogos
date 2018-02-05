/*
 * @(#)AbstractNxt.java
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

import java.awt.Component;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.lego.BrickUtils;

/**
 * @author dabo
 * 
 */
public abstract class AbstractNxt
    implements Nxt {

  private final Object programExecutionLock = new Object();


  public static Collection<BrickDescription<? extends Nxt>> getAvailableBricks(
      Component parent,
            ProgressListener progress, AtomicBoolean cancel, PrintWriter log) {

    Collection<BrickDescription<? extends Nxt>> infos =
      new ArrayList<BrickDescription<? extends Nxt>>();

    ProgressEvent evt = new ProgressEvent(AbstractNxt.class, null, 0, 0, 0);
    if (progress != null) {
      progress.progressChanged(evt);
    }

    Collection<BrickFactory<? extends Nxt>> factories =
      new ArrayList<BrickFactory<? extends Nxt>>();
    try {
      factories.add(NxtFantom.getFactory());
    } catch (Exception exn) {
      if (log != null) {
        log.println(exn);
      }
    }
    /*
     * try { factories.add(NxtSerial.getFactory()); } catch (Exception exn) {
     * System.err.println(exn); }
     */

    Map<BrickFactory<? extends Nxt>, String[]> ports =
      new HashMap<BrickFactory<? extends Nxt>, String[]>();
    int numPorts = 0;
    for (BrickFactory<? extends Nxt> factory : factories) {
      if (cancel.get()) {
        break;
      }

      try {
        String[] p = factory.getAvailablePorts();
        if (p != null) {
          numPorts += p.length;
          ports.put(factory, p);
        }
      } catch (Exception exn) {
        if (log != null) {
          log.println(exn);
        }
      }
    }

    evt.setEnd(numPorts);

    for (BrickFactory<? extends Nxt> factory : ports.keySet()) {
      for (final String port : ports.get(factory)) {
        if (cancel.get()) {
          break;
        }
        evt.setMessage(port);
        if (progress != null) {
          progress.progressChanged(evt);
        }
        try {
          infos.add(factory.getBrickInfo(parent, port));
        } catch (Exception exn) {
          if (log != null) {
            log.println(exn);
          }
        }
        evt.setCurrent(evt.getCurrent() + 1);
        if (progress != null) {
          progress.progressChanged(evt);
        }
      }
    }

    return infos;
  }


  @Override
  protected void finalize()
      throws Throwable {

    this.close();
    super.finalize();
  }


  abstract protected byte[] sendDirectCommand(byte[] command,
      int expectedResponseSize)
        throws IOException;


  public int getBatteryLevel()
      throws IOException {

    byte[] answer = this.sendDirectCommand(new byte[] { 0x0B }, 3);
    AbstractNxt.checkStatus(answer);

    return (int)BrickUtils.readNum(answer, 1, 2, false);
  }


  /**
   * Play a tone.
   * 
   * @param frequency
   *          the frequency of the tone (200 - 14000 Hz)
   * @param duration
   *          the duration in ms
   * @throws IOException
   */
  public void playTone(int frequency, int duration)
      throws IOException {

    this.playTone(frequency, duration, true);
  }


  public void playTone(int frequency, int duration, boolean wait)
      throws IOException {

    if (frequency > 0xFFFF) {
      throw new IllegalArgumentException("Frequency out of range: " + frequency);
    }
    if (duration > 0xFFFF) {
      throw new IllegalArgumentException("Frequency out of range: " + frequency);
    }
    this.sendDirectCommand(new byte[] { 0x03, (byte)(frequency & 0xff),
      (byte)(frequency >> 8),
                (byte)(duration & 0xff), (byte)(duration >> 8) }, 0);

    try {
      if (wait) {
        Thread.sleep(duration);
      }
    } catch (InterruptedException exn) {
      // ignore
    }
  }


  public void playSoundFile(String filename, boolean loop)
      throws IOException {

    int dot = filename.indexOf(".");
    if (dot > 0) {
      if (!filename.substring(dot).equals(Nxt.SOUND_EXTENSION)) {
        throw new IllegalArgumentException("Illegal sound file name: "
          + filename);
      }
    }
    else {
      filename += Nxt.SOUND_EXTENSION;
    }

    if (filename.length() >= 15 + Nxt.SOUND_EXTENSION.length() + 1) {
      throw new IllegalArgumentException("File name too long");
    }

    byte[] cmd = new byte[22];
    cmd[0] = 0x02;
    cmd[1] = (byte)(loop ? 0x01 : 0x00);
    for (int i = 0; i < filename.length(); i++) {
      cmd[2 + i] = (byte)filename.charAt(i);
    }
    for (int i = 2 + filename.length(); i < cmd.length; i++) {
      cmd[i] = 0x00;
    }

    byte[] answer = this.sendDirectCommand(cmd, 1);
    AbstractNxt.checkStatus(answer);
  }


  public void stopSoundPlayback()
      throws IOException {

    this.sendDirectCommand(new byte[] { 0x0C }, 0);
  }


  public long keepAlive()
      throws IOException {

    if (true) {
      return -1;
    }

    synchronized (this.programExecutionLock) {
      if (this.getCurrentProgram() != null) {
        return -1;
      }
      else {
        byte[] answer = this.sendDirectCommand(new byte[] { 0x0D }, 5);
        AbstractNxt.checkStatus(answer);

        return BrickUtils.readNum(answer, 1, 4, false);
      }
    }
  }


  public void startProgram(String filename)
      throws IOException {

    int dot = filename.indexOf(".");
    if (dot > 0) {
      if (!filename.substring(dot).equals(Nxt.PROGRAM_EXTENSION)) {
        throw new IllegalArgumentException("Illegal program name: " + filename);
      }
    }
    else {
      filename += Nxt.PROGRAM_EXTENSION;
    }

    if (filename.length() >= 15 + Nxt.PROGRAM_EXTENSION.length() + 1) {
      throw new IllegalArgumentException("File name too long");
    }

    byte[] cmd = new byte[21];
    cmd[0] = 0x00;
    for (int i = 0; i < filename.length(); i++) {
      cmd[1 + i] = (byte)filename.charAt(i);
    }
    for (int i = 1 + filename.length(); i < cmd.length; i++) {
      cmd[i] = 0x00;
    }

    synchronized (this.programExecutionLock) {
      byte[] answer = this.sendDirectCommand(cmd, 1);
      AbstractNxt.checkStatus(answer);
    }
  }


  public boolean stopProgram()
      throws IOException {

    synchronized (this.programExecutionLock) {
      byte[] answer = this.sendDirectCommand(new byte[] { 0x01 }, 1);
      if (answer[0] == MessageStatusException.NO_ACTIVE_PROGRAM) {
        return false;
      }
      else {
        AbstractNxt.checkStatus(answer);
        return true;
      }
    }
  }


  public String getCurrentProgram()
      throws IOException {

    byte[] answer = this.sendDirectCommand(new byte[] { 0x11 }, 21);
    if (answer[0] == (byte)0xEC) {
      // no active program
      return null;
    }
    else {
      AbstractNxt.checkStatus(answer);

      return BrickUtils.readString(answer, 1, 20);
    }
  }


  public void runProgram(String filename)
      throws IOException {

    this.startProgram(filename);
    try {
      while (this.getCurrentProgram() != null) {
        Thread.sleep(50);
      }
    } catch (InterruptedException exn) {
      this.stopProgram();
    }
  }


  public void setSensorType(int sensor, Sensor.Type type, Sensor.Mode mode,
      int slope)
        throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }
    if ((slope < 0) || (slope > 31)) {
      throw new IllegalArgumentException("Illegal slope for RCX sensor mode");
    }

    int code = mode.getValue() + slope;

    // set type and mode
    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x05, (byte)sensor,
        (byte)type.getValue(),
                (byte)code }, 1);
    AbstractNxt.checkStatus(answer);
  }


  public Sensor.Type getSensorType(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal NXT sensor");
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x07, (byte)sensor }, 14);
    AbstractNxt.checkStatus(answer);

    int type = (int)BrickUtils.readNum(answer, 4, 1, false);
    for (Sensor.Type t : Sensor.Type.values()) {
      if (t.getValue() == type) {
        return t;
      }
    }
    return null;
  }


  public Sensor.Mode getSensorMode(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal NXT sensor");
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x07, (byte)sensor }, 14);
    AbstractNxt.checkStatus(answer);

    int mode =
      (int)BrickUtils.readNum(answer, 5, 1, false) & Sensor.Mode.MODE_MASK;
    for (Sensor.Mode m : Sensor.Mode.values()) {
      if (m.getValue() == mode) {
        return m;
      }
    }
    return null;
  }


  public int getSensorValue(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x07, (byte)sensor }, 14);
    AbstractNxt.checkStatus(answer);

    return (int)BrickUtils.readNum(answer, 10, 2, false);
  }


  public int getSensorRawValue(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x07, (byte)sensor }, 14);
    AbstractNxt.checkStatus(answer);

    return (int)BrickUtils.readNum(answer, 6, 2, false);
  }


  public byte[] lsRead(int sensor, byte[] data, int expectedResultSize)
      throws IOException {

    if ((sensor < 0) || (sensor > 3)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }

    if ((data != null) && (data.length > 0)) {
      byte[] cmd = new byte[data.length + 4];
      cmd[0] = 0x0F;
      cmd[1] = (byte)sensor;
      cmd[2] = (byte)data.length;
      cmd[3] = (byte)expectedResultSize;
      System.arraycopy(data, 0, cmd, 4, data.length);
      byte[] answer = this.sendDirectCommand(cmd, 1);
      AbstractNxt.checkStatus(answer);
    }

    if (expectedResultSize == 0) {
      return null;
    }
    else {
      long timeout = 5000;
      long time = System.currentTimeMillis();
      byte[] getStatus = new byte[] { 0x0E, (byte)sensor };
      byte[] read = new byte[] { 0x10, (byte)sensor };

      int pos = 0;
      byte[] result = new byte[expectedResultSize];
      while (pos < expectedResultSize) {
        int bytesReady = 0;
        while (bytesReady == 0) {
          if (System.currentTimeMillis() > time + timeout) {
            throw new NiVisaStatusException(NiVisaStatusException.VI_ERROR_TMO);
          }
          byte[] answer = this.sendDirectCommand(getStatus, 2);
          if ((answer[0] == MessageStatusException.CHANNEL_BUSY)
                            || (answer[0] == MessageStatusException.TRANSACTION_IN_PROGRESS)) {
            bytesReady = 0;
          }
          else {
            AbstractNxt.checkStatus(answer);
            bytesReady = (int)BrickUtils.readNum(answer, 1, 1, false);
          }
        }
        byte[] answer = this.sendDirectCommand(read, 18);
        AbstractNxt.checkStatus(answer);
        bytesReady = (int)BrickUtils.readNum(answer, 1, 1, false);
        System.arraycopy(answer, 2, result, pos, bytesReady);
        pos += bytesReady;
        // System.out.println("returning " + pos + " bytes");
        break;
      }
      return result;
    }
  }


  public void resetMotorPosition(Motor.Port motor, boolean relative)
      throws IOException {

    if (motor == null) {
      throw new IllegalArgumentException();
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x0A, (byte)motor.getID(),
                (byte)(relative ? 0x01 : 0x00) }, 1);
    AbstractNxt.checkStatus(answer);
  }


  public void setOutputState(Motor.Port motor, int power, int mode,
      Motor.Regulation regulation,
            int turnRatio, Motor.State state, long tachoLimit)
      throws IOException {

    if (motor == null) {
      throw new IllegalArgumentException();
    }

    byte cmd[] = new byte[12];
    cmd[0] = 0x04;
    cmd[1] = (byte)motor.getID();
    cmd[2] = (byte)power;
    cmd[3] = (byte)mode;
    cmd[4] = (byte)regulation.getValue();
    cmd[5] = (byte)turnRatio;
    cmd[6] = (byte)state.getValue();

    cmd[7] = (byte)((tachoLimit >> 0) & 0xFF);
    cmd[8] = (byte)((tachoLimit >> 8) & 0xFF);
    cmd[9] = (byte)((tachoLimit >> 16) & 0xFF);
    cmd[10] = (byte)((tachoLimit >> 24) & 0xFF);
    cmd[11] = (byte)((tachoLimit >> 32) & 0xFF);

    byte[] answer = this.sendDirectCommand(cmd, 1);
    AbstractNxt.checkStatus(answer);
  }


  public MotorState getOutputState(Motor.Port motor)
      throws IOException {

    if (motor == null) {
      throw new IllegalArgumentException();
    }

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x06, (byte)motor.getID() }, 23);
    AbstractNxt.checkStatus(answer);

    int power = (int)BrickUtils.readNum(answer, 2, 1, false);
    int mode = (int)BrickUtils.readNum(answer, 3, 1, false);
    int regulation = (int)BrickUtils.readNum(answer, 4, 1, false);
    int turnRation = (int)BrickUtils.readNum(answer, 5, 1, false);
    int state = (int)BrickUtils.readNum(answer, 6, 1, false);
    long tachoLimit = BrickUtils.readNum(answer, 7, 4, false);
    int tachoCount = (int)BrickUtils.readNum(answer, 11, 4, false);
    int blockTachoCount = (int)BrickUtils.readNum(answer, 15, 4, false);
    int rotationCount = (int)BrickUtils.readNum(answer, 19, 4, false);

    return new MotorState(state, mode, regulation, power, turnRation,
      tachoLimit, tachoCount,
            blockTachoCount, rotationCount);
  }


  public void sendMessage(int mailbox, String message)
      throws IOException {

    if ((mailbox < 0) || (mailbox > 9)) {
      throw new IllegalArgumentException("Illegal mailbox id");
    }

    byte[] msg = message.getBytes();
    if (msg.length > 58) {
      throw new IllegalArgumentException("Message too long");
    }

    byte[] command = new byte[msg.length + 4];
    command[0] = 0x09;
    command[1] = (byte)mailbox;
    command[2] = (byte)(message.length() + 1);

    System.arraycopy(msg, 0, command, 3, msg.length);
    // add null termination
    command[command.length - 1] = 0;

    byte[] answer = this.sendDirectCommand(command, 1);
    AbstractNxt.checkStatus(answer);
  }


  @Override
  public String toString() {

    try {
      NxtDeviceInfo info = this.getDeviceInfo();
      return info.getName();
    } catch (IOException exn) {
      return super.toString();
    }
  }


  protected static void checkStatus(byte[] answer)
      throws IOException {

    byte result = answer[0];
    if (result != 0) {
      throw new MessageStatusException(result);
    }
  }
}
