package com.clt.util;

import javax.swing.JMenuBar;

/**
 * This class contains routines to implement specific, platform dependent
 * behaviour.
 */

public class Platform {

  private static OS platform;
  private static boolean leaveSpaceForGrowBox;
  private static boolean showQuitMenuItem;

  public static Object ToolboxLock;

  static {
    String p = System.getProperty("os.name").toLowerCase();
    if (p.indexOf("win") >= 0) {
      Platform.platform = OS.Windows;
    }
    else if (p.indexOf("mac") >= 0) {
      Platform.platform = OS.MacOS;
    }
    else if (p.indexOf("linux") >= 0) {
      Platform.platform = OS.Linux;
    }
    else {
      Platform.platform = OS.Unknown;
    }

    p = System.getProperty("com.clt.gui.growboxes.intrude", "false");
    Platform.leaveSpaceForGrowBox = p.equalsIgnoreCase("true");

    p = System.getProperty("com.clt.gui.quit.enabled", null);
    if (p == null) {
      Platform.showQuitMenuItem = !Platform.isMac();
    }
    else {
      Platform.showQuitMenuItem = p.equalsIgnoreCase("true");
    }

    Platform.ToolboxLock = new Object();
  }

  private static int toolboxLockCount = 0;


  public static void beginToolboxCallback() {

    Platform.toolboxLockCount++;
  }


  public static void endToolboxCallback() {

    Platform.toolboxLockCount--;
  }


  public static void acquireToolboxLock() {

    if (Platform.toolboxLockCount == 0) {
      if (Platform.isMac()) {
        try {
          Class<?> c = Class.forName("com.apple.mrj.macos.carbon.CarbonLock");
          c.getMethod("acquire", new Class[0]).invoke(null, new Object[0]);
        } catch (ThreadDeath e) {
          // wird bei System.exit() geworfen. Muss laut Java
          // Spezifikation
          // immer weitergegeben werden.
          throw e;
        } catch (Throwable t) {
          // couldn't aquire lock. No problem, this is legacy code anyways.
        }
      }
    }
    Platform.toolboxLockCount++;
  }


  public static void releaseToolboxLock() {

    Platform.toolboxLockCount--;
    if (Platform.toolboxLockCount == 0) {
      if (Platform.isMac()) {
        try {
          Class<?> c = Class.forName("com.apple.mrj.macos.carbon.CarbonLock");
          c.getMethod("release", (Class[])null).invoke(null, (Object[])null);
        } catch (ThreadDeath e) {
          // wird bei System.exit() geworfen. Muss laut Java
          // Spezifikation
          // immer weitergegeben werden.
          throw e;
        } catch (Throwable t) {
          // couldn't release lock. No problem, this is legacy code anyways.
        }
      }
    }
  }


  public static OS getPlatform() {

    return Platform.platform;
  }


  public static boolean isMac() {

    return Platform.platform == OS.MacOS;
  }


  public static boolean isWindows() {

    return Platform.platform == OS.Windows;
  }


  public static boolean isLinux() {

    return Platform.platform == OS.Linux;
  }
  
  public static boolean is64Bit() {
      return "64".equals(System.getProperty("sun.arch.data.model"));
  }


  public static boolean leaveSpaceForGrowBox() {

    return Platform.leaveSpaceForGrowBox;
  }


  public static boolean showQuitMenuItem() {

    return Platform.showQuitMenuItem;
  }


  /** @deprecated This was old, now obsolete code for the Mac OS L&amp;F on Mac OS 9. */
  @Deprecated
  public static void setSecondaryMenuBar(JMenuBar mbar) {

    // code removed, this is a noop
  }

}