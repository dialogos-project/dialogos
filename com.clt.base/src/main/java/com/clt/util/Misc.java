package com.clt.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.clt.resources.DynamicResourceBundle;

public class Misc {

  private static DynamicResourceBundle resources = new DynamicResourceBundle(
        "com.clt.util.Resources");


  /**
   * Return a localized version of the given string.
   */
  public static String getString(String key) {

    return Misc.resources.getString(key);
  }


  public static String format(String key, Object... strings) {

    return StringTools.format(Misc.getString(key), strings);
  }


  /**
   * Convenience method to make the current thread sleep without the need to
   * catch InterruptedExceptions.
   * 
   * @param ms
   */
  public static void sleep(long ms) {

    try {
      Thread.sleep(ms);
    } catch (InterruptedException ignore) {
      // ignore the fact that we were interrupted
    }
  }


  public static void dumpThreads() {

    System.out.println("########## Thread dump ##########");
    Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
    for (Thread t : threads.keySet()) {
      System.out.println(t.getName());
    }
  }


  private static byte[] getMacAddress(NetworkInterface nif)
      throws UnsupportedOperationException {

    byte[] macAddress = null;

    try {
      boolean isLoopback =
        (Boolean)nif.getClass().getMethod("isLoopback", (Class[])null).invoke(
                nif, (Object[])null);
      boolean isP2P =
        (Boolean)nif.getClass().getMethod("isPointToPoint", (Class[])null)
          .invoke(
                nif, (Object[])null);
      if (!isLoopback && !isP2P) {
        macAddress =
          (byte[])nif.getClass().getMethod("getHardwareAddress", (Class[])null)
            .invoke(
                  nif, (Object[])null);
      }
    } catch (Exception ignore) {
      throw new UnsupportedOperationException();
    }

    return macAddress;
  }


  public static byte[] getMacAddress()
      throws UnsupportedOperationException, IOException {

    byte[] macAddress = null;
    NetworkInterface nif =
      NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
    macAddress = Misc.getMacAddress(nif);
    if (macAddress == null) {
      Enumeration<NetworkInterface> nifs =
        NetworkInterface.getNetworkInterfaces();
      while ((macAddress == null) && nifs.hasMoreElements()) {
        macAddress = Misc.getMacAddress(nifs.nextElement());
      }
    }
    return macAddress;
  }


  /**
   * Return the OSType interpretation of <code>s</code>. The OSType is an int
   * which is interpreted as a sequence of four 8bit characters. If
   * <code>s</code> contains unicode characters which cannot be represented in 8
   * bit, this method will throw an IllegalArgumentException.
   */
  public static int OSType(String s) {

    if (s.length() != 4) {
      throw new IllegalArgumentException("OSType strings must have length 4");
    }

    int result = 0;
    for (int i = 0; i < 4; i++) {
      char c = s.charAt(i);
      if (c > 255) {
        throw new IllegalArgumentException(
                  "OSType strings must contain only 8bit characters");
      }
      result = result << 8;
      result = result | (c & 0x00FF);
    }

    return result;
  }


  /**
   * Check whether the given array contains the object <code>o</code>.
   * <code>o</code> may be <code>null</code>.
   */
  public static boolean contains(Object array[], Object o) {

    return Misc.indexOf(array, o) != -1;
  }


  /**
   * Return the position of <code>o</code> in the given array. <code>o</code>
   * may be <code>null</code>.
   * 
   * @return The index <code>n</code> if <code>array[n].equals(o)</code> of
   *         <code>-1</code> if no string in the array equals <code>o</code>
   */
  public static int indexOf(Object array[], Object o) {

    for (int i = 0; i < array.length; i++) {
      if (o == null ? array[i] == null : o.equals(array[i])) {
        return i;
      }
    }
    return -1;
  }


  /** Remove elements at the given indices from <code>v</code>. */
  public static void removeElements(List<?> v, int indices[]) {

    int i, j;
    boolean sorted;

    // Sicherstellen, dass das Array sortiert ist.
    for (i = 0; i < indices.length; i++) {
      sorted = true;
      for (j = 1; j < indices.length - i; j++) {
        if (indices[j] < indices[j - 1]) {
          int temp = indices[j - 1];
          indices[j - 1] = indices[j];
          indices[j] = temp;
          sorted = false;
        }
      }
      if (sorted) {
        break;
      }
    }

    // Elemente von hinten beginnend entfernen, damit sich
    // die Indices nicht verschieben.
    for (i = indices.length - 1; i >= 0; i--) {
      v.remove(indices[i]);
    }
  }


  /*
   * public static URL FileToURL(File f) { try { if (f != null ? f.exists() :
   * false) { String path = f.getAbsolutePath(); if (File.separatorChar != '/')
   * { path = path.replace(File.separatorChar, '/'); } if
   * (!path.startsWith("/")) { path = "/" + path; } if (!path.endsWith("/") &&
   * f.isDirectory()) { path = path + "/"; } return new URL("file", "", "//" +
   * path); } } catch (Exception e) {} return null; }
   */

  /**
   * @deprecated This method was only introduced to implement correct
   *             comparisons of files and collections of these. You should
   *             instead override java.io.File to use
   *             {@link #equals(File, File)}.
   */
  @Deprecated
  @SuppressWarnings("unchecked")
  public static boolean equals(Object o1, Object o2) {

    if (o1 == o2) {
      return true;
    }
    else if ((o1 == null) || (o2 == null)) {
      return false;
    }
    else if ((o1 instanceof File) && (o2 instanceof File)) {
      return Misc.equals((File)o1, (File)o2);
    }
    else if ((o1 instanceof Collection) && (o2 instanceof Collection)) {
      return Misc.equals((Collection)o1, (Collection)o2);
    }
    else if ((o1 instanceof Map) && (o2 instanceof Map)) {
      return Misc.equals((Map)o1, (Map)o2);
    }
    else {
      return o1.equals(o2);
    }
  }


  /**
   * Compare two files by comparing their canonical paths. This ensures that two
   * files are considered equal, even if one is described by a relativ path and
   * the other by an absolute path.
   */
  public static boolean equals(File f1, File f2) {

    if (f1 == f2) {
      return true;
    }
    else if ((f1 == null) || (f2 == null)) {
      return false;
    }

    try {
      return f1.getCanonicalPath().equals(f2.getCanonicalPath());
    } catch (IOException e) {
      return f1.equals(f2);
    }
  }


  /**
   * @deprecated This method was only introduced to implement correct
   *             comparisons of files and collections of these. You should
   *             instead override java.io.File to use
   *             {@link #equals(File, File)}.
   */
  @Deprecated
  public static boolean equals(Collection<?> v1, Collection<?> v2) {

    if (v1 == v2) {
      return true;
    }
    else if ((v1 == null) || (v2 == null)) {
      return false;
    }

    if (v1.size() != v2.size()) {
      return false;
    }

    Iterator<?> e1 = v1.iterator(), e2 = v2.iterator();

    while (e1.hasNext()) {
      if (!Misc.equals(e1.next(), e2.next())) {
        return false;
      }
    }

    // ok, keine Unterschiede gefunden.
    return true;
  }


  /**
   * @deprecated This method was only introduced to implement correct
   *             comparisons of files and collections of these. You should
   *             instead override java.io.File to use
   *             {@link #equals(File, File)}.
   */
  @Deprecated
  public static boolean equals(Map<?, ?> h1, Map<?, ?> h2) {

    // kleiner Check zum beschleunigen
    if (h1 == h2) {
      return true;
    }
    else if ((h1 == null) || (h2 == null)) {
      return false;
    }

    // wenn Groesse unterschiedlich koennen wir direkt aufhoeren
    if (h1.size() != h2.size()) {
      return false;
    }

    // Test: sind die Keys und Values identisch?
    Iterator<?> e = h1.keySet().iterator();

    while (e.hasNext()) {
      Object key = e.next();
      if (!h1.get(key).equals(h2.get(key))) {
        return false;
      }
    }

    // ok, keine Unterschiede gefunden.
    return true;
  }


  public static File getApplicationDirectory() {

    String appPath = System.getProperty("com.clt.appDir", null);
    if (appPath == null) {
      appPath = System.getProperty("user.dir", null);
    }
    if (appPath == null) {
      appPath = ".";
    }

    return new File(appPath);
  }
}