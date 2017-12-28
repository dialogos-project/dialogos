package com.clt.lego.nxt;

import java.awt.Component;
import java.io.IOException;

import com.clt.gui.OptionPane;
import com.clt.io.InterfaceType;
import com.clt.lego.BrickDescription;
import com.clt.lego.BrickFactory;
import com.clt.util.UserCanceledException;

public class NxtFantom
    extends AbstractNxt {

  public static void main(String... args) {

    try {
      NxtFantom.link();

      String names[] = NxtFantom.getAvailableBricks(3);
      if (names.length == 0) {
        System.err.println("No NXT brick found");
      }

      for (String name : names) {
        System.out.println("Found " + name);
      }

      for (String name : names) {
        for (int n = 0; n < 1; n++) {
          Nxt nxt = NxtFantom.createBrick(null, name);
          System.out.println("Brick URI " + nxt.getResourceString());
          System.out.println(nxt.getDeviceInfo());

          System.out.println("Battery level: " + nxt.getBatteryLevel());

          System.out.println("Modules: ");
          for (String module : nxt.getModules()) {
            System.out.println("  " + module);
          }

          System.out.println("Display:");
          /*
           * System.out.println(java.util.Arrays.toString(nxt.nReadIOMap(nxt.handle
           * , "Display.mod"))); nxt.keepAlive();
           */

          /*
           * byte[] lineData = new byte[100]; for (int i = 0; i <
           * lineData.length; i++) lineData[i] = (byte) 0x02; for (int i = 319;
           * i <= 419; i += 100) nxt.writeIOMap("Display.mod", i, lineData); for
           * (int i = 0; i < lineData.length; i++) lineData[i] = (byte) ((1 <<
           * (i % 8))); for (int i = 519; i <= 619; i += 100)
           * nxt.writeIOMap("Display.mod", i, lineData);
           */

          Display display = new Display(nxt, false);
          display.drawLine(0, 0, display.getWidth(), display.getHeight());
          display.drawLine(10, 10, 20, 20);
          display.drawLine(10, 20, 20, 30);
          display.drawLine(10, 30, 20, 40);
          display.drawString("Hello world", 10, 40);
          display.flush();

          byte[] displayData = nxt.readIOMap("Display.mod", 0, 0);
          boolean bigEndian = false;
          System.out.println("PFunc: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 0, 4, bigEndian), 8));
          System.out.println("EraseMask: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 4, 4, bigEndian), 8));
          System.out.println("UpdateMask: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 8, 4, bigEndian), 8));
          System.out.println("PFont: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 12, 4, bigEndian), 8));

          for (int i = 0; i < 8; i++) {
            System.out.println("Line "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 16 + 4 * i, 4,
                                        bigEndian), 8));
          }
          System.out.println("StatusText: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 48, 4, bigEndian), 8));
          System.out.println("StatusIcons: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 52, 4, bigEndian), 8));

          for (int i = 0; i < 3; i++) {
            System.out.println("PScreen "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 56 + 4 * i, 4,
                                        bigEndian), 8));
          }
          for (int i = 0; i < 4; i++) {
            System.out.println("PBitmaps "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 68 + 4 * i, 4,
                                        bigEndian), 8));
          }

          System.out.println("PMenuText: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 84, 4, bigEndian), 8));
          for (int i = 0; i < 3; i++) {
            System.out.println("PMenuIcon "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 88 + 4 * i, 4,
                                        bigEndian), 8));
          }
          System.out.println("PStepIcons: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 100, 4, bigEndian), 8));
          System.out.println("Display: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 104, 4, bigEndian), 8));
          for (int i = 0; i < 4; i++) {
            System.out.println("StatusIcon "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 108 + i, 1,
                                        bigEndian), 2));
          }
          for (int i = 0; i < 5; i++) {
            System.out.println("StepIcon "
                                + (i + 1)
                                + ": "
                                + com.clt.util.StringTools.toHexString(
                                    com.clt.lego.BrickUtils.readNum(
                                      displayData, 112 + i, 1,
                                        bigEndian), 2));
          }
          System.out.println("Flags: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 117, 1, bigEndian), 2));
          System.out.println("TextLinesCenterFlags: "
                            + com.clt.util.StringTools.toHexString(
                              com.clt.lego.BrickUtils.readNum(
                                displayData, 118, 1, bigEndian), 2));
          for (int line = 0; line < 8; line++) {
            System.out.print("Line " + (line + 1) + ":");

            for (int pos = 0; pos < 100; pos++) {
              if ((pos % 20 == 0) && (pos > 0)) {
                System.out.println();
                System.out.print("       ");
              }
              System.out.print(" "
                                    + com.clt.util.StringTools.toHexString(
                                        com.clt.lego.BrickUtils.readNum(
                                          displayData, 119
                                                + (line * 100) + pos, 1,
                                          bigEndian), 2));
            }
            System.out.println();
          }

          // new
          // Display(nxt).paint(com.clt.gui.Images.loadBuiltin("CLT.png").getImage());

          /*
           * for (int i = 0; i < 50; i++) {
           * System.out.println("Center button state: " + new
           * Buttons(nxt).getState(Buttons.Button.CENTER)); Thread.sleep(200); }
           */

          /*
           * Sensor s = new Sensor(nxt, Sensor.Port.S1);
           * s.setType(Sensor.Type.I2C_9V, Sensor.Mode.RAW);
           * 
           * for (int i = 0; i < 10; i++) { System.out.println("Sensor 1: " +
           * s.getValue() + " (raw value: " + s.getRawValue() + ")");
           * Thread.sleep(200); }
           */

          /*
           * System.out.println("Starting motor A"); Motor m = new Motor(nxt,
           * Motor.Port.A); m.setPower(80); m.forward();
           * 
           * //nxt.motorOn(Motor.A, 100, -180); Thread.sleep(2000); m.drift();
           * Thread.sleep(1000); m.backward(); Thread.sleep(2000); m.stop();
           * System.out.println("Done");
           */

          /*
           * nxt.resetMotorPosition(Motor.Port.A, false); new Motor(nxt,
           * Motor.Port.A).forward(80, 1000);
           * 
           * for (int i = 0; i < 100; i++) {
           * //System.out.println(nxt.getOutputState(Motor.Port.A));
           * System.out.println("Actual speed: " + new
           * Output(nxt).getActualPower(Motor.Port.A) + " (" + new
           * Output(nxt).getRPM(Motor.Port.A) + " RPM, Overloaded = " + new
           * Output(nxt).isOverloaded(Motor.Port.A) + ")"); Thread.sleep(50); }
           * 
           * new Motor(nxt, Motor.Port.A).drift();
           */

          // nxt.playSound(440, 500, true);
          // nxt.playSound(880, 500, true);
          nxt.close();
        }
      }
    } catch (Exception exn) {
      exn.printStackTrace();
      System.exit(1);
    }
    System.exit(0);
  }

  private static boolean libraryLoaded = false;
  private static final Object driverLock = new Object();

  private int handle;
  private InterfaceType interfaceType;


  private NxtFantom(int handle, InterfaceType interfaceType) {

    if (handle < 0) {
      throw new IllegalArgumentException();
    }

    this.handle = handle;
    this.interfaceType = interfaceType;
  }


  public void close() {

    synchronized (NxtFantom.driverLock) {
      if (this.handle != -1) {
        this.nDispose(this.handle);
        this.handle = -1;
      }
    }
  }


  public InterfaceType getInterfaceType() {

    return this.interfaceType;
  }


  public String getPort() {

    try {
      String uri = this.getResourceString();
      if (this.getInterfaceType() == InterfaceType.Bluetooth) {
        int index = uri.lastIndexOf("::");
        if ((index >= 0) && (index + 2 < uri.length())) {
          // make sure it is a number
          int port = Integer.parseInt(uri.substring(index + 2));
          return String.valueOf(port);
        }
      }
    } catch (Exception exn) {
      // ignore
    }
    return null;
  }


  @Override
  protected byte[] sendDirectCommand(byte[] command, int expectedResponseSize)
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.sendDirectCommand(this.handle, command, expectedResponseSize);
    }
  }


  public String[] getPrograms()
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nGetFiles(this.handle, "*" + Nxt.PROGRAM_EXTENSION);
    }
  }


  public String[] getModules()
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nGetModules(this.handle);
    }
  }


  public int getModuleID(String name)
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nGetModuleID(this.handle, name);
    }
  }


  public NxtDeviceInfo getDeviceInfo()
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nGetDeviceInfo(this.handle);
    }
  }


  public String getResourceString()
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nGetResourceString(this.handle);
    }
  }


  public byte[] readIOMap(String module, int offset, int length)
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      return this.nReadIOMap(this.handle, module, offset, length);
    }
  }


  public void writeIOMap(String module, int offset, byte[] data)
      throws IOException {

    synchronized (NxtFantom.driverLock) {
      this.nWriteIOMap(this.handle, module, offset, data);
    }
  }


  private static void link()
      throws IOException {

    if (!NxtFantom.libraryLoaded) {
      try {
        System.loadLibrary("LegoLib");
        NxtFantom.libraryLoaded = true;
      } catch (Throwable error) {
        String msg = error.getLocalizedMessage();
        if ((msg != null) && (msg.length() > 0)) {
          throw new IOException(msg);
        }
        else {
          throw new IOException("The NXT driver could not be loaded");
        }
      }
    }
  }


  public static NxtFantom createBrick(Component parent, String uri)
        throws IOException, UserCanceledException {

    NxtFantom.link();

    synchronized (NxtFantom.driverLock) {
      if (!NxtFantom.isPaired(uri)) {
        String passkey =
          OptionPane.edit(parent,
                    Resources.getString("EnterAndConfirmPassword"),
            "Bluetooth Pairing for " + uri,
                    "1234");
        if (passkey == null) {
          throw new UserCanceledException();
        }
        else {
          uri = NxtFantom.pairBluetooth(uri, passkey);
        }
      }

      return new NxtFantom(NxtFantom.nCreateNxt(uri), uri.startsWith("USB")
        ? InterfaceType.USB
                    : InterfaceType.Bluetooth);
    }
  }


  public static BrickFactory<NxtFantom> getFactory()
      throws IOException {

    NxtFantom.link();

    return new BrickFactory<NxtFantom>() {

      public String[] getAvailablePorts()
          throws IOException {

        synchronized (NxtFantom.driverLock) {
          return NxtFantom.getAvailableBricks(5);
        }
      }


      public BrickDescription<NxtFantom> getBrickInfo(Component parent,
          String uri)
                throws IOException, UserCanceledException {

        synchronized (NxtFantom.driverLock) {
          NxtFantom nxt = NxtFantom.createBrick(parent, uri);
          BrickDescription<NxtFantom> info;
          try {
            InterfaceType type = nxt.getInterfaceType();
            info =
              new Description(uri, nxt.getDeviceInfo(), type, nxt.getPort());
          } finally {
            nxt.close();
          }
          return info;
        }
      }

    };
  }


  public static boolean isPaired(String uri)
      throws IOException {

    NxtFantom.link();

    synchronized (NxtFantom.driverLock) {
      return NxtFantom.nIsPaired(uri);
    }
  }


  public static String pairBluetooth(String uri, String passkey)
      throws IOException {

    NxtFantom.link();

    synchronized (NxtFantom.driverLock) {
      return NxtFantom.nPairBluetooth(uri, passkey);
    }
  }


  /**
   * Return an array of URIs for all available NXT bricks
   * 
   * @param bluetoothTimeout
   *          The timeout in seconds or 0 to disable Bluetooth scanning
   * @return An array of URIs
   * @throws IOException
   */
  private static native String[] getAvailableBricks(int bluetoothTimeout)
      throws IOException;


  /**
   * Return whether NXT brick at the given URI is already paired. Only
   * meaningful for bricks connected via Bluetooth. For bricks connected via
   * USB, this method will return true.
   **/
  private static native boolean nIsPaired(String uri)
      throws IOException;


  private static native String nPairBluetooth(String uri, String passkey)
      throws IOException;


  private static native int nCreateNxt(String uri)
      throws IOException;


  private native void nDispose(int handle);


  private native byte[] sendDirectCommand(int handle, byte[] command,
      int expectedResponseSize)
        throws IOException;


  private native String[] nGetFiles(int handle, String filter)
      throws IOException;


  private native String[] nGetModules(int handle)
      throws IOException;


  private native byte[] nReadIOMap(int handle, String module, int offset,
      int length)
        throws IOException;


  private native void nWriteIOMap(int handle, String module, int offset,
      byte[] data)
        throws IOException;


  private native int nGetModuleID(int handle, String module)
      throws IOException;


  private native NxtDeviceInfo nGetDeviceInfo(int handle)
      throws IOException;


  private native String nGetResourceString(int handle)
      throws IOException;

  public static class Description
        extends BrickDescription<NxtFantom> {

    public Description(String uri, NxtDeviceInfo brickInfo, InterfaceType type,
        String port) {

      super(uri, brickInfo, type, port);
    }


    @Override
    protected NxtFantom createBrickImpl(Component parent)
            throws IOException, UserCanceledException {

      NxtFantom nxt = NxtFantom.createBrick(parent, this.getURI());
      this.uri = nxt.getResourceString();
      return nxt;
    }
  }

}
