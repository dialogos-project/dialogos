package com.clt.gui.menus;

public class AbstractMenuCommander
    implements MenuCommander {

  public String menuItemName(int cmd, String oldName) {

    return oldName;
  }


  public boolean menuItemState(int cmd) {

    return true;
  }


  public boolean doCommand(int cmd) {

    return false;
  }
}