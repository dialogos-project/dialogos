package com.clt.gui.menus;

import com.clt.gui.Commander;

public interface MenuCommander extends Commander {

    public boolean menuItemState(int cmd);

    public String menuItemName(int cmd, String oldName);
}
