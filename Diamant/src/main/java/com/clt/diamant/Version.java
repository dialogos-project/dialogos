/*
 * @(#)Version.java
 * Created on Wed Jul 21 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.diamant;

import java.awt.Component;
import java.net.InetAddress;

import com.clt.gui.AboutDialog;
import com.clt.gui.OptionPane;
import com.clt.util.StringTools;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Version {

  public static boolean HICOLOR = false;
  public static boolean ANIMATE = false;
  public static boolean DEBUG = false;

  public static final String PRODUCT_NAME = "DialogOS";

  public static final int MAJOR_VERSION = 1;
  public static final int MINOR_VERSION = 3;
  public static final int BUGFIX_VERSION = 99;
  public static final String NONRELEASE = "beta1";

  public static String getVersion() {

    StringBuffer b = new StringBuffer();
    b.append(Version.MAJOR_VERSION);
    b.append('.');
    b.append(Version.MINOR_VERSION);
    if (Version.BUGFIX_VERSION != 0) {
      b.append('.');
      b.append(Version.BUGFIX_VERSION);
    }
    if (Version.NONRELEASE != null) {
      b.append(Version.NONRELEASE);
    }
    return b.toString();
  }


  public static void showAboutDialog() {

    String credits = // "<b>Das Entwickler-Team:\n\n" +
      "<b>design\nDaniel Bobbert\nPhilipp Detemple\nGerhard Fliedner\nC.J. Rupp\n\n"
                + "<b>implementation\nDaniel Beck\nDaniel Bobbert\n\n"
                + "<b>testing\nAnastasia Ammann\nMatthias Bauer\nDaniel Beck\nDaniel Bobbert\nPhilipp Detemple\nGerd Fliedner\nClaudia Grote\nHajo Keffer\nIris Kersten\nRoland Roller\nMichael Roth\nSarah Schmitt\nDiana Steffen";
    new AboutDialog(
            Version.PRODUCT_NAME,
            Version.getVersion(),
            "\u00A9 CLT Sprachtechnologie GmbH"
              + "\nThis product includes software\n"
                    + "developed by the Apache Software\n"
              + "Foundation (http://www.apache.org/)",
            credits).show(null);
  }
}
