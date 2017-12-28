/*
 * @(#)RcxNative.java
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

import java.awt.Component;
import java.io.IOException;

import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.lego.BrickInfo;

/**
 * @author dabo
 * 
 */
public class RcxNative
    extends AbstractRcx {

  private Tower tower;


  private static RcxNative createBrick(Component parent, String uri)
      throws IOException {

    return new RcxNative(uri);
  }


  private RcxNative(String port)
      throws TowerException {

    this.tower = new Tower(port);
  }


  public String getPort() {

    return this.tower.getPort();
  }


  @Override
  protected byte[] sendDirectCommandImpl(byte[] command,
      int expectedResponseSize)
        throws IOException {

    byte[] response = new byte[expectedResponseSize];
    this.tower.sendPacketReceivePacket(command, response, 4);
    return response;
  }


  public void close()
      throws IOException {

    this.tower.dispose();
  }


  @Override
  public String getResourceString() {

    return this.tower.getPort();
  }


  public InterfaceType getInterfaceType() {

    try {
      return this.tower.isUSB() ? InterfaceType.USB : InterfaceType.Serial;
    } catch (Exception exn) {
      return null;
    }
  }


  private static void link()
      throws IOException {

    Tower.initLibrary();
  }


  public static BrickFactory<Rcx> getFactory()
      throws IOException {

    RcxNative.link();

    return new BrickFactory<Rcx>() {

      public String[] getAvailablePorts() {

        return new String[] { "usb" };
      }


      public BrickDescription<Rcx> getBrickInfo(Component parent, String port)
                throws IOException {

        Rcx rcx = RcxNative.createBrick(parent, port);
        BrickDescription<Rcx> info;
        try {
          InterfaceType type = rcx.getInterfaceType();
          BrickInfo brickInfo = rcx.getDeviceInfo();
          info = new Description(port, brickInfo, type, rcx.getPort());
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
    protected RcxNative createBrickImpl(Component parent)
        throws IOException {

      return RcxNative.createBrick(parent, this.getURI());
    }
  }
}
