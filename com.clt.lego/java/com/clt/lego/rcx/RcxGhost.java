/*
 * @(#)RcxGhost.java
 * Created on 25.06.2007 by dabo
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
import java.util.ArrayList;
import java.util.List;

import com.clt.io.InterfaceType;
import com.clt.lego.Brick;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickException;
import com.clt.lego.BrickFactory;

/**
 * @author dabo
 * 
 */
public class RcxGhost
    extends AbstractRcx {

  private static final String PROTOCOL_USB = "usb:";
  private static final String PROTOCOL_SERIAL = "serial:";

  private static boolean libraryLoaded = false;
  private static final Object driverLock = new Object();

  private int stack;
  private InterfaceType interfaceType;
  private String uri;


  public static void initLibrary()
      throws IOException {

    RcxGhost.link();
  }


  private static void link()
      throws IOException {

    if (!RcxGhost.libraryLoaded) {
      try {
        System.loadLibrary("LegoLib");
        RcxGhost.nInitLibrary();
        RcxGhost.libraryLoaded = true;
      } catch (Throwable error) {
        String msg = error.getLocalizedMessage();
        if ((msg != null) && (msg.length() > 0)
          && !(error instanceof UnsatisfiedLinkError)) {
          throw new BrickException(msg, error);
        }
        else {
          throw new BrickException("Could not load LegoLib", error);
        }
      }
    }
  }


  private static RcxGhost createBrick(Component parent, String uri)
      throws IOException {

    if (uri.startsWith(RcxGhost.PROTOCOL_USB)) {
      return new RcxGhost(true, uri.substring(RcxGhost.PROTOCOL_USB.length()));
    }
    else if (uri.startsWith(RcxGhost.PROTOCOL_SERIAL)) {
      return new RcxGhost(false, uri.substring(RcxGhost.PROTOCOL_SERIAL
        .length()));
    }
    else {
      throw new IOException("Illegal device URI " + uri);
    }
  }


  public static BrickFactory<RcxGhost> getFactory()
      throws IOException {

    RcxGhost.link();

    return new BrickFactory<RcxGhost>() {

      public String[] getAvailablePorts() {

        synchronized (RcxGhost.driverLock) {
          List<String> bricks = new ArrayList<String>();
          try {
            RcxGhost usb = new RcxGhost(true, null);
            for (String uri : usb.getAvailableBricks()) {
              bricks.add(RcxGhost.PROTOCOL_USB + uri);
            }
            usb.close();
          } catch (Exception exn) {
            System.err.println(exn);
          }

          try {
            RcxGhost serial = new RcxGhost(false, null);
            for (String uri : serial.getAvailableBricks()) {
              bricks.add(RcxGhost.PROTOCOL_SERIAL + uri);
            }
            serial.close();
          } catch (Exception exn) {
            System.err.println(exn);
          }

          return bricks.toArray(new String[bricks.size()]);
        }
      }


      public BrickDescription<RcxGhost> getBrickInfo(Component parent,
          String uri)
                throws IOException {

        synchronized (RcxGhost.driverLock) {
          RcxGhost brick = RcxGhost.createBrick(parent, uri);
          BrickDescription<RcxGhost> info;
          try {
            InterfaceType type = brick.getInterfaceType();
            RcxDeviceInfo deviceInfo = brick.getDeviceInfo();
            info = new Description(uri, deviceInfo, type, brick.getPort());
          } finally {
            brick.close();
          }
          return info;
        }
      }
    };
  }


  private RcxGhost(boolean usb, String uri)
      throws IOException {

    String port, protocol, session;

    if (usb) {
      port = "LEGO.Pbk.CommStack.Port.USB";
    }
    else {
      port = "LEGO.Pbk.CommStack.Port.RS232";
    }

    protocol = "LEGO.Pbk.CommStack.Protocol.IR";

    session = "LEGO.Pbk.CommStack.Session";

    synchronized (RcxGhost.driverLock) {
      this.stack = this.nCreateStack(port, protocol, session);
      this.interfaceType = usb ? InterfaceType.USB : InterfaceType.Serial;
      this.uri = uri;

      if (uri != null) {
        this.nOpenDevice(this.stack, uri);

        if (usb) {
          System.out.println(this.nDeviceControl(this.stack, "copyright=?"));
          System.out.println("Version = "
            + this.nDeviceControl(this.stack, "version=?"));
          System.out.println("Range   = "
            + this.nDeviceControl(this.stack, "range=?"));
          System.out.println("Mode    = "
            + this.nDeviceControl(this.stack, "mode=?"));
          System.out.println("Endian  = "
            + this.nDeviceControl(this.stack, "endianness=?"));
          System.out.println("Speed(r)= "
            + this.nDeviceControl(this.stack, "irspeed[rx]=?"));
          System.out.println("Speed(t)= "
            + this.nDeviceControl(this.stack, "irspeed[tx]=?"));
        }
      }
    }
  }


  public void close()
      throws GhostException {

    synchronized (RcxGhost.driverLock) {
      if (this.stack != -1) {
        if (this.uri != null) {
          this.nCloseDevice(this.stack);
          this.uri = null;
        }

        this.nCloseStack(this.stack);
        this.stack = -1;
      }
    }
  }


  public String getPort() {

    return null;
  }


  @Override
  public String getResourceString() {

    return (this.getInterfaceType() == InterfaceType.USB
      ? RcxGhost.PROTOCOL_USB : RcxGhost.PROTOCOL_SERIAL)
      + this.uri;
  }


  @Override
  protected byte[] sendDirectCommandImpl(byte[] command,
      int expectedResponseSize)
        throws GhostException {

    synchronized (RcxGhost.driverLock) {
      return this.nSendDirectCommand(this.stack, command, expectedResponseSize);
    }
  }


  public InterfaceType getInterfaceType() {

    return this.interfaceType;
  }


  private String[] getAvailableBricks()
      throws GhostException {

    synchronized (RcxGhost.driverLock) {
      return this.nGetDevices(this.stack);
    }
  }


  private static native void nInitLibrary();


  private final native int nCreateStack(String port, String protocol,
      String session)
        throws GhostException;


  private final native void nCloseStack(int handle)
      throws GhostException;


  private final native String[] nGetDevices(int handle)
      throws GhostException;


  private final native void nOpenDevice(int handle, String device)
      throws GhostException;


  private final native void nCloseDevice(int handle)
      throws GhostException;


  private final native byte[] nSendDirectCommand(int stack2, byte[] command,
            int expectedResponseSize)
      throws GhostException;


  private final native String nDeviceControl(int stack2, String command)
      throws GhostException;


  @SuppressWarnings("unused")
  private static final native void nSetDebug(boolean debug);

  public static class Description
        extends BrickDescription<RcxGhost> {

    public Description(String uri, RcxDeviceInfo brickInfo, InterfaceType type,
        String port) {

      super(uri, brickInfo, type, port);
    }


    @Override
    protected RcxGhost createBrickImpl(Component parent)
        throws IOException {

      return RcxGhost.createBrick(parent, this.getURI());
    }
  }


  public static void main(String... args) {

    try {
      RcxGhost.link();

      BrickFactory<? extends Brick> factory = RcxGhost.getFactory();
      String names[] = factory.getAvailablePorts();
      if (names.length == 0) {
        System.err.println("No brick found");
      }

      for (String name : names) {
        System.out.println("Found " + name);
      }

      for (String name : names) {
        for (int n = 0; n < 2; n++) {
          BrickDescription<? extends Brick> brickInfo =
            factory.getBrickInfo(null, name);
          System.out.println("Brick URI " + brickInfo.getURI());
          name = brickInfo.getBrickName();
          System.out.println("Brick name " + name);

          Brick brick = brickInfo.createBrick(null);
          try {
            brick.keepAlive();

            System.out.println(brick.getDeviceInfo());

            if (brick instanceof com.clt.lego.nxt.Nxt) {
              com.clt.lego.nxt.Nxt nxt = (com.clt.lego.nxt.Nxt)brick;
              String[] files = nxt.getPrograms();
              for (String file : files) {
                System.out.println("  " + file);
              }
              System.out.println("Current program: " + nxt.getCurrentProgram());
            }
            else if (brick instanceof Rcx) {
              Rcx rcx = (Rcx)brick;

              if (brick.getDeviceInfo().getFirmwareVersion() == 0) {
                ((AbstractRcx)brick).uploadFirmware();
              }

              rcx.playSound(RcxSound.UpwardSweep);
              System.out.println("Current program: " + rcx.getCurrentProgram());
              rcx.showNumber(-34);
            }

            System.out.println("Battery level: " + brick.getBatteryLevel());
            System.out.println("Sleep time: " + brick.keepAlive());

            brick.keepAlive();

            if (brick instanceof com.clt.lego.nxt.Nxt) {
              ((com.clt.lego.nxt.Nxt)brick).startProgram("Demo");
              // Thread.sleep(1000);
              for (int i = 0; i < 5; i++) {
                System.out.println("Current program: "
                                        + ((com.clt.lego.nxt.Nxt)brick)
                                          .getCurrentProgram());
                ((com.clt.lego.nxt.Nxt)brick).keepAlive();

              }
              ((com.clt.lego.nxt.Nxt)brick).stopProgram();
              System.out.println("Current program: "
                                    + ((com.clt.lego.nxt.Nxt)brick)
                                      .getCurrentProgram());
              ((com.clt.lego.nxt.Nxt)brick).keepAlive();
            }
            // nxt.playSound(440, 500, true);
            // nxt.playSound(880, 500, true);
          } finally {
            brick.close();
          }
        }
      }
    } catch (Exception exn) {
      exn.printStackTrace();
      System.exit(1);
    }
    System.exit(0);
  }
}
