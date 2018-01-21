package com.clt.gui.menus;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.clt.gui.Commands;

public class CmdMenuBar extends JMenuBar implements MenuOwner {

    private MenuCommander commander;

    public CmdMenuBar() {
        this(new AbstractMenuCommander());
    }

    public CmdMenuBar(MenuCommander c) {
        this.commander = c;
    }

    public CmdMenu addMenu(String name) {

        return this.addMenu(name, Commands.cmdMenu);
    }

    public CmdMenu addMenu(String name, int cmdNum) {

        CmdMenu menu = new CmdMenu(name, cmdNum, this.commander);
        this.add(menu);
        return menu;
    }

    public void updateMenus() {

        for (int i = this.getMenuCount() - 1; i >= 0; i--) {
            JMenu m = this.getMenu(i); // may return null
            if (m != null) {
                CmdMenuBar.updateMenu(m);
            }
        }
    }

    static void updateMenu(JMenu menu) {

        for (int i = menu.getItemCount() - 1; i >= 0; i--) {
            JMenuItem item = menu.getItem(i);

            if (item instanceof CmdMenuItem) {
                ((CmdMenuItem) item).update();
            } else if (item instanceof CmdCheckBoxMenuItem) {
                ((CmdCheckBoxMenuItem) item).update();
            } else if ((item instanceof JMenu) && (item != menu)) {
                CmdMenuBar.updateMenu((JMenu) item);
            }
        }

        if (menu instanceof CmdMenu) {
            ((CmdMenu) menu).update();
        }
    }

}
