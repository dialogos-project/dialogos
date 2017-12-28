/*
 * @(#)Main.java
 * Created on Thu Dec 12 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

/**
 * Launcher for the DialogClient main application.
 * 
 * @author dabo
 */
public class Main {

  public static void main(String args[]) {

    if (System.getProperty("apple.laf.useScreenMenuBar") == null) {
      System.setProperty("apple.laf.useScreenMenuBar", "true");
    }
    if (System.getProperty("com.apple.macos.useScreenMenuBar") == null) {
      System.setProperty("com.apple.macos.useScreenMenuBar", "true");
    }
    if (System.getProperty("com.apple.mrj.application.apple.menu.about.name") == null) {
      System.setProperty("com.apple.mrj.application.apple.menu.about.name",
        "DialogClient");
    }

    GUIClientFactory.main(args);
  }
}
