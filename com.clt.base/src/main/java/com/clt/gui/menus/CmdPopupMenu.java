/*
 * @(#)CmdPopupMenu.java
 * Created on Tue Jan 14 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui.menus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;

import com.clt.gui.Commands;
import com.clt.gui.Images;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class CmdPopupMenu
    extends JPopupMenu
    implements MenuOwner {

  private MenuCommander commander;


  public CmdPopupMenu(MenuCommander commander) {

    this(null, commander);
  }


  public CmdPopupMenu(String name, MenuCommander commander) {

    super(name);
    this.commander = commander;
  }


  public JMenuItem addItem(String cmdName, int cmdNum) {

    return this.addItem(cmdName, cmdNum, -1);
  }


  public JMenuItem addItem(String cmdName, int cmdNum, int cmdKey) {

    return this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
  }


  public JMenuItem addItem(String cmdName, int cmdNum, KeyStroke cmdKey) {

    return this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
  }


  public JMenuItem addItem(String cmdName, int cmdNum, char Mnemonic) {

    JMenuItem item = this.addItem(cmdName, cmdNum, -1);
    item.setMnemonic(Mnemonic);
    return item;
  }


  public JMenuItem addItem(String cmdName, int cmdNum, int cmdKey, char Mnemonic) {

    JMenuItem item =
      this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
    item.setMnemonic(Mnemonic);
    return item;
  }


  public JMenuItem addItem(String cmdName, int cmdNum, KeyStroke cmdKey,
      char Mnemonic) {

    JMenuItem item =
      this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
    item.setMnemonic(Mnemonic);
    return item;
  }


  public JMenuItem addItem(String name, String icon, int cmdNum) {

    JMenuItem item = this.addItem(name, cmdNum);
    item.setIcon(Images.load(icon));
    return item;
  }


  public CmdMenu addSubMenu(String name) {

    return this.addSubMenu(name, Commands.cmdMenu);
  }


  public CmdMenu addSubMenu(String name, int cmdNum) {

    CmdMenu m = new CmdMenu(name, cmdNum, this.commander);
    this.add(m);
    return m;
  }


  public void updateMenus() {

    MenuElement[] items = this.getSubElements();
    for (int i = 0; i < items.length; i++) {
      if (items[i] instanceof CmdMenuItem) {
        ((CmdMenuItem)items[i]).update();
      }
      else if (items[i] instanceof CmdCheckBoxMenuItem) {
        ((CmdCheckBoxMenuItem)items[i]).update();
      }
      else if (items[i] instanceof JMenu) {
        CmdMenuBar.updateMenu((JMenu)items[i]);
      }
    }
  }
}