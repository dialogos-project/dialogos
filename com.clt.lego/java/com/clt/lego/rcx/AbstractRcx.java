/*
 * @(#)AbstractRcx.java
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.clt.lego.BrickException;
import com.clt.lego.BrickUtils;
import com.clt.util.StringTools;

/**
 * @author dabo
 * 
 */
public abstract class AbstractRcx
    implements Rcx {

  private static final byte IMMEDIATE_VALUE = 2;

  private static final byte REPEAT_BIT = 0x08;
  private static final byte REPEAT_MASK = ~AbstractRcx.REPEAT_BIT;


  @Override
  protected void finalize()
      throws Throwable {

    this.close();
    super.finalize();
  }


  final protected byte[] sendDirectCommand(byte[] command,
      int expectedResponseSize)
        throws IOException {

    byte[] answer =
      this.sendDirectCommandImpl(command, expectedResponseSize + 1);

    if ((answer[0] & AbstractRcx.REPEAT_MASK) != (~command[0] & AbstractRcx.REPEAT_MASK)) {
      String error = "Warning: Expected return package "
                    + StringTools.toHexString(~command[0], 2) + " but got "
                    + StringTools.toHexString(answer[0], 2);
      System.err.println(error);
    }

    byte[] result = new byte[expectedResponseSize];
    if (expectedResponseSize > 0) {
      System.arraycopy(answer, 1, result, 0, expectedResponseSize);
    }
    return result;
  }


  abstract protected byte[] sendDirectCommandImpl(byte[] command,
      int expectedResponseSize)
        throws IOException;


  public long keepAlive()
      throws IOException {

    this.sendDirectCommand(new byte[] { 0x10 }, 0);

    // byte[] answer = sendDirectCommand(new byte[] { }, expectedResponseSize);
    return 0;
  }


  public int getBatteryLevel()
      throws IOException {

    byte[] answer = this.sendDirectCommand(new byte[] { 0x30 }, 2);

    return (int)BrickUtils.readNum(answer, 0, 2, false);
  }


  public int[] getMemoryMap()
      throws IOException {

    byte[] answer = this.sendDirectCommand(new byte[] { 0x20 }, 188);

    int[] result = new int[answer.length / 2];
    for (int i = 0; i < result.length; i++) {
      result[i] = (int)BrickUtils.readNum(answer, 2 * i, 2, true);
    }
    return result;
  }


  public void setDisplayMode(DisplayMode display)
      throws IOException {

    this.sendDirectCommand(new byte[] { 0x33, AbstractRcx.IMMEDIATE_VALUE,
      (byte)display.getValue(), 0 }, 0);
  }


  public void showNumber(int number)
      throws IOException {

    this.sendDirectCommand(new byte[] { (byte)0xE5, 0, 0,
      AbstractRcx.IMMEDIATE_VALUE, (byte)(number & 0xFF),
                (byte)(number >> 8) }, 0);
  }


  public void setTime(byte hours, byte minutes)
      throws IOException {

    if ((hours < 0) || (hours > 23)) {
      throw new IllegalArgumentException("Illegal value for hours");
    }
    if ((minutes < 0) || (minutes > 59)) {
      throw new IllegalArgumentException("Illegal value for minutes");
    }

    this.sendDirectCommand(new byte[] { 0x22, hours, minutes }, 0);
  }


  public void setSensorType(int sensor, Sensor.Type type, Sensor.Mode mode,
      int slope)
        throws IOException {

    if ((sensor < 0) || (sensor > 2)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }
    if ((slope < 0) || (slope > 31)) {
      throw new IllegalArgumentException("Illegal slope for RCX sensor mode");
    }

    // set type
    this.sendDirectCommand(new byte[] { 0x32, (byte)sensor,
      (byte)type.getValue() }, 0);

    int code = mode.getValue() << 5;
    code += slope;

    // set mode
    this.sendDirectCommand(new byte[] { 0x42, (byte)sensor, (byte)code }, 0);
  }


  public void setMotorPower(int value, Motor.Port... motors)
      throws IOException {

    if ((value < 0) || (value > 7)) {
      throw new IllegalArgumentException("Illegal value for RCX motor power");
    }

    int mask = 0;
    for (Motor.Port m : motors) {
      mask |= m.getID();
    }

    this.sendDirectCommand(new byte[] { 0x13, (byte)mask,
      AbstractRcx.IMMEDIATE_VALUE, (byte)value }, 0);
  }


  public void setMotorDirection(Motor.Direction direction, Motor.Port... motors)
        throws IOException {

    int mask = 0;
    for (Motor.Port m : motors) {
      mask |= m.getID();
    }

    mask |= direction.getValue();

    this.sendDirectCommand(new byte[] { (byte)0xE1, (byte)mask }, 0);
  }


  public void setMotorState(Motor.State state, Motor.Port... motors)
      throws IOException {

    int mask = 0;
    for (Motor.Port m : motors) {
      mask |= m.getID();
    }

    mask |= state.getValue();

    this.sendDirectCommand(new byte[] { 0x21, (byte)mask }, 0);
  }


  private int getValue(int source, int argument)
      throws IOException {

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x12, (byte)source, (byte)argument },
        2);

    return (int)BrickUtils.readNum(answer, 0, 2, false);
  }


  public int getSensorValue(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 2)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }

    return this.getValue(9, sensor);
  }


  public int getSensorRawValue(int sensor)
      throws IOException {

    if ((sensor < 0) || (sensor > 2)) {
      throw new IllegalArgumentException("Illegal RCX sensor");
    }

    return this.getValue(12, sensor);
  }


  public RcxDeviceInfo getDeviceInfo()
      throws IOException {

    byte[] answer =
      this.sendDirectCommand(new byte[] { 0x15, 1, 3, 5, 7, 11 }, 8);
    int romVersion = (int)BrickUtils.readNum(answer, 0, 4, true);
    int firmwareVersion = (int)BrickUtils.readNum(answer, 4, 4, true);

    return new RcxDeviceInfo(this.getResourceString(), firmwareVersion,
      romVersion);
  }


  public abstract String getResourceString();


  public void playSound(RcxSound sound)
      throws IOException {

    this.sendDirectCommand(new byte[] { 0x51, (byte)sound.getValue() }, 0);
  }


  public void playTone(int frequency, int duration)
      throws IOException {

    this.sendDirectCommand(new byte[] { 0x23, (byte)(frequency & 0xFF),
      (byte)(frequency >> 8),
                (byte)(duration / 10) }, 0);
  }


  public void setProgramNumber(int program)
      throws IOException {

    if ((program < 0) || (program > 4)) {
      throw new IllegalArgumentException("Illegal program number");
    }

    this.sendDirectCommand(new byte[] { (byte)0x91, (byte)program }, 0);
  }


  public int getCurrentProgram()
      throws IOException {

    return this.getValue(8, 0);
  }


  public int getVariable(int variable)
      throws IOException {

    if ((variable < 0) || (variable > 31)) {
      throw new IllegalArgumentException("Illegal variable index: " + variable);
    }

    return this.getValue(0, variable);
  }


  public void setVariable(int variable, int value)
      throws IOException {

    if ((variable < 0) || (variable > 31)) {
      throw new IllegalArgumentException("Illegal variable index: " + variable);
    }

    this.sendDirectCommand(new byte[] { 0x14, (byte)variable,
      AbstractRcx.IMMEDIATE_VALUE,
                (byte)(value & 0xff), (byte)(value >> 8) }, 0);
  }


  public void uploadFirmware()
      throws IOException {

    InputStream firmware =
      this.getClass().getClassLoader().getResourceAsStream(
            this.getClass().getPackage().getName().replace('.', '/')
              + "/firm0328.lgo");
    if (firmware == null) {
      throw new IOException("Firmware resources missing");
    }
    byte[] data = new byte[firmware.available()];
    new DataInputStream(firmware).readFully(data);
    this.uploadFirmware(data);
  }


  public void uploadFirmware(byte[] data)
      throws IOException {

    int startAddr = 0x8000;
    int length = data.length;
    int offset = 0;

    SRec firmware = new SRec(data);

    startAddr = firmware.getStart();
    data = firmware.getData();
    length = firmware.getLength();

    // go into boot mode
    this.sendDirectCommand(new byte[] { (byte)0x65, 1, 3, 5, 7, 11 }, 0);

    // calculate checksum of first 19k modulo 65536
    int checksum = 0;
    for (int i = 0; (i < length) && (i < 19456); i++) {
      int x = data[offset + i];
      if (x < 0) {
        x += 256;
      }
      checksum += x;
      if (checksum > 65536) {
        checksum -= 65536;
      }
    }

    // prepare upload
    byte[] answer =
      this.sendDirectCommand(new byte[] { (byte)0x75, (byte)(startAddr & 0xFF),
                (byte)(startAddr >> 8), (byte)(checksum & 0xff),
        (byte)(checksum >> 8), 0 }, 1);
    if (answer[0] != 0) {
      throw new BrickException("Could not start firmware upload");
    }

    // send data blocks
    int n = 0;
    int blockNumber = 1;
    int blockSize = 0;
    System.out.println("Transferring " + length + " bytes:");
    for (n = 0; n < length; n += blockSize) {
      blockSize = Math.min(length - n, 512);
      if (n + blockSize >= length) {
        blockNumber = 0;
      }
      byte[] block = new byte[blockSize + 6];
      block[0] = 0x45;
      block[1] = (byte)(blockNumber & 0xFF);
      block[2] = (byte)(blockNumber >> 8);
      block[3] = (byte)(blockSize & 0xFF);
      block[4] = (byte)(blockSize >> 8);

      int blockCRC = 0;
      for (int i = 0; i < blockSize; i++) {
        block[5 + i] = data[offset + n + i];
        blockCRC += data[offset + n + i];
        if (blockCRC < 0) {
          blockCRC += 256;
        }
        else if (blockCRC > 256) {
          blockCRC -= 256;
        }
      }
      block[5 + blockSize] = (byte)blockCRC;

      answer = this.sendDirectCommand(block, 1);
      if (answer[0] == 3) {
        throw new BrickException("Block checksum error");
      }
      else if (answer[0] == 4) {
        throw new BrickException("Firmware checksum error");
      }
      if (answer[0] == 6) {
        throw new BrickException("Firmware upload not active");
      }

      blockNumber++;
      System.out.print("\r" + (n * 100 / length) + "%");
    }

    System.out.println("\r100%");
    System.out.println("Unlocking firmware");
    // unlock firmware
    String response = "Just a bit off the block!";
    answer =
      this.sendDirectCommand(new byte[] { (byte)0xA5, (byte)'L', (byte)'E',
        (byte)'G',
                (byte)'O', (byte)0xAE }, response.length());
    if (!BrickUtils.readString(answer, 0, response.length()).equals(response)) {
      throw new BrickException("Firmware could not be activated");
    }
  }
}
