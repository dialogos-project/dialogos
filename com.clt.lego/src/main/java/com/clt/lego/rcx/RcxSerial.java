/*
 * @(#)RcxSerial.java
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

import java.awt.Component;
import java.io.IOException;

import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.lego.BrickInfo;
import com.clt.lego.SerialPort;

/**
 * @author dabo
 * 
 */
public class RcxSerial
    extends AbstractRcx {

  private boolean usb = true;
  private byte lastOpcode = 0;

  private SerialPort port;


  private static RcxSerial createBrick(Component parent, String uri)
      throws IOException {

    return new RcxSerial(uri);
  }


  private RcxSerial(String port)
      throws IOException {

    this.usb = port.equalsIgnoreCase("usb");
    this.port = new SerialPort(port);
    this.port.openForRcx();
  }


  public String getPort() {

    return this.usb ? null : this.port.getPortname();
  }


  @Override
  public String getResourceString() {

    return "RCX";
  }


  @Override
  protected byte[] sendDirectCommandImpl(byte[] command,
      int expectedResponseSize)
        throws IOException {

    byte[] message = new byte[command.length * 2 + 5];

    message[0] = (byte)0x55;
    message[1] = (byte)0xFF;
    message[2] = (byte)0x00;

    message[3] = command[0];
    if (command[0] == this.lastOpcode) {
      message[3] |= (byte)0x08;
    }
    message[4] = (byte)~message[3];
    this.lastOpcode = command[0];

    int sum = RcxSerial.unsigned(message[3]);
    for (int i = 1; i < command.length; i++) {
      message[3 + 2 * i] = command[i];
      message[4 + 2 * i] = (byte)~command[i];

      sum += RcxSerial.unsigned(command[i]);
    }

    message[3 + 2 * command.length] = (byte)(sum & 0xFF);
    message[4 + 2 * command.length] = (byte)(~sum & 0xFF);

    this.port.getOutputStream().write(message);

    byte[] response = new byte[message.length + 2 * expectedResponseSize];
    this.port.getInputStream().read(response);

    for (int i = 0; i < message.length - 2; i++) {
      if (response[i] != message[i]) {
        throw new IOException("echo mismatch");
      }
    }

    byte[] answer = new byte[expectedResponseSize];
    for (int i = 0; i < expectedResponseSize; i++) {
      answer[i] = response[message.length - 2 + 2 * i];
      if ((byte)~answer[i] != response[message.length - 2 + 2 * i + 1]) {
        throw new IOException("complement check failed");
      }
    }

    if ((byte)~response[response.length - 2] != response[response.length - 1]) {
      throw new IOException("complement check failed");
    }

    sum = 0;
    for (int i = 3; i < response.length - 2; i++) {
      sum += RcxSerial.unsigned(response[i]);
    }

    if (RcxSerial.unsigned(response[response.length - 2]) != (sum & 0xFF)) {
      throw new IOException("checksum error");
    }

    return answer;
  }


  private static int unsigned(byte b) {

    int i = b;
    if (i < 0) {
      i += 256;
    }
    return i;
  }


  public void close() {

    if (this.port != null) {
      this.port.close();
    }
  }


  public InterfaceType getInterfaceType() {

    return this.usb ? InterfaceType.USB : InterfaceType.Serial;
  }


  private static void link()
      throws IOException {

    try {
      RcxSerial.class.getClassLoader().loadClass("gnu.io.CommPort");
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


  public static BrickFactory<Rcx> getFactory()
      throws IOException {

    RcxSerial.link();

    return new BrickFactory<Rcx>() {

      public String[] getAvailablePorts() {

        return SerialPort.getAvailablePorts();
      }


      public BrickDescription<Rcx> getBrickInfo(Component parent, String port)
                throws IOException {

        Rcx rcx = RcxSerial.createBrick(parent, port);
        BrickDescription<Rcx> info;
        try {
          InterfaceType type = rcx.getInterfaceType();
          info =
            new Description(port, rcx.getDeviceInfo(), type, rcx.getPort());
        } finally {
          rcx.close();
        }
        return info;
      }
    };
  }

  private static class Description
        extends BrickDescription<Rcx> {

    public Description(String uri, BrickInfo brickInfo, InterfaceType type,
        String port) {

      super(uri, brickInfo, type, port);
    }


    @Override
    protected Rcx createBrickImpl(Component parent)
        throws IOException {

      return RcxSerial.createBrick(parent, this.getURI());
    }
  }
}
