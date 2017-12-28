/*
 * @(#)NxtSerial.java
 * Created on 13.04.2007 by dabo
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
import java.util.ArrayList;
import java.util.Collection;

import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.lego.BrickUtils;
import com.clt.lego.SerialPort;

/**
 * @author dabo
 * 
 */
public class NxtSerial
    extends AbstractNxt {

  private SerialPort port;


  private NxtSerial(String port)
      throws IOException {

    this.port = new SerialPort(port);
    this.port.openForNxt();
  }


  public String getPort() {

    return this.port.getPortname();
  }


  public String getResourceString() {

    return this.port.getPortname();
  }


  public void close() {

    if (this.port != null) {
      this.port.close();
    }
  }


  @Override
  protected byte[] sendDirectCommand(byte[] command, int expectedResponseSize)
      throws IOException {

    boolean includeLengthHeader =
      this.getInterfaceType() == InterfaceType.Bluetooth;
    int offset = includeLengthHeader ? 2 : 0;

    byte[] cmd = new byte[command.length + 1 + offset];

    if (includeLengthHeader) {
      int msgSize = command.length + 1;
      cmd[0] = (byte)(msgSize & 0xFF);
      cmd[1] = (byte)(msgSize >>> 8);
    }
    cmd[offset] = expectedResponseSize > 0 ? (byte)0x00 : (byte)0x80;
    System.arraycopy(command, 0, cmd, offset + 1, command.length);

    this.port.getOutputStream().write(cmd);

    if (expectedResponseSize > 0) {
      byte[] response = new byte[expectedResponseSize];
      int answer = this.port.getInputStream().read();
      if (answer != 0x02) {
        throw new IOException();
      }
      answer = this.port.getInputStream().read();
      if (answer != command[0]) {
        throw new IOException("First byte of answer is not the command id");
      }
      this.port.getInputStream().read(response);
      return response;
    }
    else {
      return null;
    }
  }


  public NxtDeviceInfo getDeviceInfo()
      throws IOException {

    if (this.getInterfaceType() == InterfaceType.Bluetooth) {
      this.port.getOutputStream().write(
        new byte[] { 0x02, 0x00, 0x01, (byte)0x9B });
    }
    else {
      this.port.getOutputStream().write(new byte[] { 0x01, (byte)0x9B });
    }

    int answer = this.port.getInputStream().read();
    if (answer != 0x02) {
      throw new IOException();
    }
    answer = this.port.getInputStream().read();
    if (answer != 0x9B) {
      throw new IOException("First byte of answer is not the command id");
    }

    byte[] response = new byte[31];
    this.port.getInputStream().read(response);
    AbstractNxt.checkStatus(response);

    String name = BrickUtils.readString(response, 1, 16);
    byte[] bluetoothAddress = new byte[6];
    System.arraycopy(response, 17, bluetoothAddress, 0, 6);
    int[] signalStrength = new int[4];
    for (int i = 0; i < 4; i++) {
      signalStrength[i] = response[23 + i];
      if (signalStrength[i] < 0) {
        signalStrength[i] += 256;
      }
    }
    int memory = (int)BrickUtils.readNum(response, 27, 4, false);

    if (this.getInterfaceType() == InterfaceType.Bluetooth) {
      this.port.getOutputStream().write(
        new byte[] { 0x02, 0x00, 0x01, (byte)0x88 });
    }
    else {
      this.port.getOutputStream().write(new byte[] { 0x01, (byte)0x88 });
    }

    answer = this.port.getInputStream().read();
    if (answer != 0x02) {
      throw new IOException();
    }
    answer = this.port.getInputStream().read();
    if (answer != 0x88) {
      throw new IOException("First byte of answer is not the command id");
    }

    response = new byte[31];
    this.port.getInputStream().read(response);
    AbstractNxt.checkStatus(response);

    int protocol = (int)BrickUtils.readNum(response, 1, 2, false);
    int firmware = (int)BrickUtils.readNum(response, 3, 2, false);

    return new NxtDeviceInfo(name, bluetoothAddress, signalStrength, memory,
      firmware, protocol);
  }


  public String[] getPrograms()
      throws IOException {

    // find first
    byte[] findFirst = new byte[22];
    findFirst[0] = 0x01;
    findFirst[1] = (byte)0x86;
    findFirst[2] = (byte)'*';
    for (int i = 0; i < Nxt.PROGRAM_EXTENSION.length(); i++) {
      findFirst[3 + i] = (byte)Nxt.PROGRAM_EXTENSION.charAt(i);
    }
    findFirst[3 + Nxt.PROGRAM_EXTENSION.length()] = 0;
    this.port.getOutputStream().write(findFirst);

    int answer = this.port.getInputStream().read();
    if (answer != 0x02) {
      throw new IOException();
    }
    answer = this.port.getInputStream().read();
    if (answer != 0x86) {
      throw new IOException("First byte of answer is not the command id");
    }

    byte[] response = new byte[26];
    this.port.getInputStream().read(response);

    Collection<String> files = new ArrayList<String>();
    while (response[0] == 0) {
      byte fileHandle = response[1];
      files.add(BrickUtils.readString(response, 2, 20));

      byte[] findNext = new byte[] { 0x01, (byte)0x87, fileHandle };
      this.port.getOutputStream().write(findNext);

      answer = this.port.getInputStream().read();
      if (answer != 0x02) {
        throw new IOException();
      }
      answer = this.port.getInputStream().read();
      if (answer != 0x87) {
        throw new IOException("First byte of answer is not the command id");
      }

      response = new byte[26];
      this.port.getInputStream().read(response);

      // close previous handle
      this.port.getOutputStream().write(
        new byte[] { 0x01, (byte)0x84, fileHandle });
      this.port.getInputStream().read(new byte[4]);
    }
    if (response[0] != (byte)0xBD) {
      AbstractNxt.checkStatus(response);
    }

    return files.toArray(new String[files.size()]);
  }


  public String[] getModules()
      throws IOException {

    // find first
    byte[] findFirst = new byte[22];
    findFirst[0] = 0x01;
    findFirst[1] = (byte)0x90;
    findFirst[2] = (byte)'*';
    for (int i = 0; i < Nxt.MODULE_EXTENSION.length(); i++) {
      findFirst[3 + i] = (byte)Nxt.MODULE_EXTENSION.charAt(i);
    }
    findFirst[3 + Nxt.MODULE_EXTENSION.length()] = 0;
    this.port.getOutputStream().write(findFirst);

    int answer = this.port.getInputStream().read();
    if (answer != 0x02) {
      throw new IOException();
    }
    answer = this.port.getInputStream().read();
    if (answer != 0x90) {
      throw new IOException("First byte of answer is not the command id");
    }

    byte[] response = new byte[32];
    this.port.getInputStream().read(response);

    Collection<String> files = new ArrayList<String>();
    while (response[0] == 0) {
      byte fileHandle = response[1];
      files.add(BrickUtils.readString(response, 2, 20));

      byte[] findNext = new byte[] { 0x01, (byte)0x91, fileHandle };
      this.port.getOutputStream().write(findNext);

      answer = this.port.getInputStream().read();
      if (answer != 0x02) {
        throw new IOException();
      }
      answer = this.port.getInputStream().read();
      if (answer != 0x91) {
        throw new IOException("First byte of answer is not the command id");
      }

      response = new byte[32];
      this.port.getInputStream().read(response);

      // close previous handle
      this.port.getOutputStream().write(
        new byte[] { 0x01, (byte)0x92, fileHandle });
      this.port.getInputStream().read(new byte[4]);
    }
    if (response[0] != (byte)0xBD) {
      AbstractNxt.checkStatus(response);
    }

    return files.toArray(new String[files.size()]);
  }


  public InterfaceType getInterfaceType() {

    return InterfaceType.Bluetooth;
  }


  public int getModuleID(String module) {

    // TODO: Implement getModuleID
    throw new UnsupportedOperationException();
  }


  public void writeIOMap(String module, int offset, byte[] data) {

    // TODO: Implement writeIOMap
    throw new UnsupportedOperationException();
  }


  public byte[] readIOMap(String module, int offset, int length) {

    // TODO: Implement readIOMap
    throw new UnsupportedOperationException();
  }


  private static void link()
      throws IOException {

    try {
      NxtSerial.class.getClassLoader().loadClass("gnu.io.CommPort");
    } catch (Throwable error) {
      String msg = error.getLocalizedMessage();
      if ((msg != null) && (msg.length() > 0)) {
        throw new IOException(msg);
      }
      else {
        throw new IOException("The serial driver could not be loaded");
      }
    }
  }


  private static NxtSerial createBrick(Component parent, String uri)
      throws IOException {

    return new NxtSerial(uri);
  }


  public static BrickFactory<Nxt> getFactory()
      throws IOException {

    NxtSerial.link();

    return new BrickFactory<Nxt>() {

      public String[] getAvailablePorts() {

        return SerialPort.getAvailablePorts();
      }


      public BrickDescription<Nxt> getBrickInfo(Component parent, String port)
                throws IOException {

        Nxt nxt = NxtSerial.createBrick(parent, port);
        BrickDescription<Nxt> info;
        try {
          InterfaceType type = nxt.getInterfaceType();
          info =
            new Description(port, nxt.getDeviceInfo(), type, nxt.getPort());
        } finally {
          nxt.close();
        }
        return info;
      }
    };
  }

  private static class Description
        extends BrickDescription<Nxt> {

    public Description(String uri, NxtDeviceInfo brickInfo, InterfaceType type,
        String port) {

      super(uri, brickInfo, type, port);
    }


    @Override
    protected NxtSerial createBrickImpl(Component parent)
        throws IOException {

      return NxtSerial.createBrick(parent, this.getURI());
    }
  }

}
