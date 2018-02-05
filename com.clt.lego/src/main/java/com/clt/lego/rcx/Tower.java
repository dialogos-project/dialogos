/*
 * @(#)Tower.java
 * Created on 04.06.2007 by dabo
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
public class Tower {

  public static final int DEFAULT_READ_TIMEOUT = 100;

  private String port;
  private boolean usb;

  /** Native handle to the active tower. */
  private int handle;

  private Object towerLock = new Object();

  private static boolean libraryLoaded = false;


  static void initLibrary()
      throws TowerException {

    if (!Tower.libraryLoaded) {
      try {
        System.loadLibrary("LegoLib");
        Tower.linkLibrary();
      } catch (Throwable exn) {
        throw new TowerException("Could not load LegoLib", exn);
      }
      Tower.libraryLoaded = true;
    }
  }


  /**
   * Create the tower class.
   * 
   * @throws TowerException
   */
  public Tower(String port)
      throws TowerException {

    this(port, false);
  }


  public Tower(String port, boolean fastMode)
      throws TowerException {

    if (port == null) {
      throw new IllegalArgumentException(
        "null port passed to Tower constructor");
    }

    Tower.initLibrary();

    synchronized (this.towerLock) {
      this.port = port;
      this.handle = this.open(port, fastMode);
      this.usb = this.isUSB(this.handle);
    }
  }


  /** Close tower. */
  public void dispose()
      throws TowerException {

    if (this.handle != -1) {
      synchronized (this.towerLock) {
        this.close(this.handle);
        this.handle = -1;
      }
    }
  }


  @Override
  public void finalize()
      throws Throwable {

    this.dispose();
    super.finalize();
  }


  public String getPort() {

    return this.port;
  }


  /**
   * Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping.
   * 
   * @param data
   *          bytes to send
   */
  public void writeBytes(byte[] data)
      throws TowerException {

    if (data == null) {
      throw new IllegalArgumentException(
        "null data array passed to Tower.writeBytes()");
    }
    if (this.handle == -1) {
      throw new TowerException("Tower disposed");
    }

    synchronized (this.towerLock) {
      this.write(this.handle, data);
    }
  }


  /**
   * Send a packet to the RCX, e.g 0x10 for ping.
   * 
   * @param data
   *          packet to send
   */
  public void sendPacket(byte[] data)
      throws TowerException {

    if (data == null) {
      throw new IllegalArgumentException(
        "null data array passed to Tower.sendPacket()");
    }
    if (this.handle == -1) {
      throw new TowerException("Tower disposed");
    }

    synchronized (this.towerLock) {
      this.send(this.handle, data);
    }
  }


  /**
   * Low-level read.
   * 
   * @param data
   *          buffer to receive bytes
   * @return number of bytes read
   */
  public int readBytes(byte[] data)
      throws TowerException {

    return this.readBytes(data, Tower.DEFAULT_READ_TIMEOUT);
  }


  /**
   * Low-level read.
   * 
   * @param data
   *          buffer to receive bytes
   * @param timeout
   *          read timeout in ms
   * @return number of bytes read
   */
  public int readBytes(byte[] data, int timeout)
      throws TowerException {

    if (data == null) {
      throw new IllegalArgumentException(
        "null data array passed to Tower.readBytes()");
    }
    if (timeout <= 0) {
      throw new IllegalArgumentException(
        "timeout must be > 0 in Tower.readBytes()");
    }
    if (this.handle == -1) {
      throw new TowerException("Tower disposed");
    }

    int result = -1;
    synchronized (this.towerLock) {
      result = this.read(this.handle, data, timeout);
      if (result < 0) {
        throw new TowerException(result);
      }
    }

    return result;
  }


  /**
   * Receive a packet.
   * 
   * @param data
   *          buffer to receive packet
   * @return number of bytes read
   */
  public int receivePacket(byte[] data)
      throws TowerException {

    return this.receivePacket(data, Tower.DEFAULT_READ_TIMEOUT);
  }


  /**
   * Receive a packet.
   * 
   * @param data
   *          buffer to receive packet
   * @param timeout
   *          read timeout in ms
   * @return number of bytes read
   */
  public int receivePacket(byte[] data, int timeout)
      throws TowerException {

    if (data == null) {
      throw new IllegalArgumentException(
        "null data array passed to Tower.receivePacket()");
    }
    if (timeout <= 0) {
      throw new IllegalArgumentException(
        "timeout must be > 0 in Tower.receivePacket()");
    }
    if (this.handle == -1) {
      throw new TowerException("Tower disposed");
    }

    int result = -1;
    synchronized (this.towerLock) {
      result = this.receive(this.handle, data, timeout);
      if (result < 0) {
        throw new TowerException(result);
      }
    }

    return result;
  }


  /**
   * Send a packet and retrieve answer.
   * 
   * @param data
   *          bytes to send
   * @param response
   *          buffer to receive packet
   * @return number of bytes read
   */
  public int sendPacketReceivePacket(byte[] data, byte[] response, int retries)
        throws TowerException {

    return this.sendPacketReceivePacket(data, response, retries,
      Tower.DEFAULT_READ_TIMEOUT);
  }


  /**
   * Send a packet and retrieve answer.
   * 
   * @param data
   *          bytes to send
   * @param response
   *          buffer to receive packet
   * @param timeout
   *          read timeout in ms
   * @return number of bytes read
   */
  public int sendPacketReceivePacket(byte[] data, byte[] response, int retries,
      int timeout)
        throws TowerException {

    if (retries <= 0) {
      throw new IllegalArgumentException("Number of (re)tries must be > 0");
    }

    synchronized (this.towerLock) {
      // reset(handle);
      if (false) {
        return this.sendPacketReceivePacket(this.handle, data, response,
          retries, timeout);
      }

      TowerException towerException = null;
      int numRead = -1;
      for (; retries > 0; retries--) {
        towerException = null;
        try {
          this.sendPacket(data);
          numRead = this.receivePacket(response, timeout);
          if (numRead < 0) {
            throw new TowerException(numRead);
          }
          break;
        } catch (TowerException e) {
          towerException = e;
          // wait 100ms before trying again
          try {
            Thread.sleep(100);
          } catch (InterruptedException e1) {
            throw new TowerException("interrupted");
          }
        }
      }

      if (towerException != null) {
        throw towerException;
      }

      return numRead;
    }
  }


  public boolean isRcxAlive()
      throws TowerException {

    synchronized (this.towerLock) {
      if (this.handle == -1) {
        throw new TowerException("Tower disposed");
      }
      else {
        return this.isRcxAlive(this.handle);
      }
    }
  }


  public boolean isUSB() {

    return this.usb;
  }


  public boolean isFastMode()
      throws TowerException {

    synchronized (this.towerLock) {
      if (this.handle == -1) {
        throw new TowerException("Tower disposed");
      }
      else {
        return this.isFastMode(this.handle);
      }
    }
  }


  //
  // native code
  //

  /** Initialize the library */
  private static native void linkLibrary()
      throws TowerException;


  /**
   * Open the tower
   * 
   * @param p
   *          port to use, e.g. usb or COM1
   * @param fastMode
   *          open port for fast mode transmissions?
   * @return a handle to the tower
   */
  protected final native int open(String port, boolean fastMode)
      throws TowerException;


  /** Close the tower */
  protected final native void close(int handle)
      throws TowerException;


  /** Check if RCX is alive */
  public final native boolean isRcxAlive(int handle)
      throws TowerException;


  /** Is tower an usb tower? */
  public final native boolean isUSB(int handle)
      throws TowerException;


  /** Is tower connected using fast mode? */
  public final native boolean isFastMode(int handle)
      throws TowerException;


  /** Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping. */
  private final native void write(int handle, byte b[])
      throws TowerException;


  /** Send a packet to the RCX, e.g 0x10 for ping. */
  private final native void send(int handle, byte b[])
      throws TowerException;


  /**
   * Low-level read.
   * 
   * @param b
   *          buffer to receive bytes
   * @param timeout
   *          read timeout in ms
   * @return number of bytes read
   */
  private final native int read(int handle, byte b[], int timeout)
      throws TowerException;


  /**
   * Receive a packet.
   * 
   * @param b
   *          buffer to receive packet
   * @param timeout
   *          read timeout in ms
   * @return number of bytes read
   */
  private final native int receive(int handle, byte b[], int timeout)
      throws TowerException;


  private final native int sendPacketReceivePacket(int handle, byte[] data,
      byte[] response,
            int retries, int timeout)
      throws TowerException;


  @SuppressWarnings("unused")
  private final native void flush(int handle)
      throws TowerException;


  @SuppressWarnings("unused")
  private final native void purge(int handle)
      throws TowerException;


  @SuppressWarnings("unused")
  private final native void reset(int handle)
      throws TowerException;


  private static final native void setDebug(boolean debug);


  public static void main(String... strings) {

    try {

      Tower t = new Tower("usb");
      // System.out.println("alive = " + t.isRcxAlive());
      System.out.println("usb = " + t.isUSB());
      System.out.println("fast = " + t.isFastMode());
      t.dispose();
      System.out.println("ok");

      Tower.initLibrary();
      Tower.setDebug(true);
      com.clt.lego.BrickFactory<Rcx> factory = RcxNative.getFactory();
      Rcx rcx = factory.getBrickInfo(null, "usb").createBrick(null);
      // rcx.keepAlive();
      System.out.println("Battery: " + rcx.getBatteryLevel() + "mV");
      // rcx.setProgramNumber(1);
      // rcx.playSound(RcxSound.UpwardSweep);
      rcx.close();
    } catch (Exception exn) {
      exn.printStackTrace();
    }
  }
}
