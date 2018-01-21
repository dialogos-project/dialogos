package com.clt.gui.menus;

public class AbstractMenuCommander implements MenuCommander {
    @Override
    public String menuItemName(int cmd, String oldName) {
        return oldName;
    }

    @Override
    public boolean menuItemState(int cmd) {
        return true;
    }

    @Override
    public boolean doCommand(int cmd) {
        return false;
    }
}
