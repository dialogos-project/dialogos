package com.clt.gui.menus;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.clt.gui.Commands;
import com.clt.gui.Images;

public class CmdMenu extends JMenu {

    private MenuCommander commander;
    private int command;

    public CmdMenu(String name, int cmdNum, MenuCommander commander) {
        super(name);
        this.commander = commander;
        this.command = cmdNum;
    }

    public CmdMenu(String name, int cmdNum, MenuCommander commander, char Mnemonic) {
        this(name, cmdNum, commander);
        this.setMnemonic(Mnemonic);
    }

    public int getCommandNumber() {

        return this.command;
    }

    public MenuCommander getCommander() {

        return this.commander;
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

        JMenuItem item
                = this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
        item.setMnemonic(Mnemonic);
        return item;
    }

    public JMenuItem addItem(String cmdName, int cmdNum, KeyStroke cmdKey,
            char Mnemonic) {

        JMenuItem item
                = this.add(new CmdMenuItem(cmdName, cmdNum, cmdKey, this.commander));
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

    void update() {

        int cmdNum = this.getCommandNumber();
        this.setEnabled(this.commander.menuItemState(cmdNum));
        String oldLabel = this.getText();
        String newLabel = this.commander.menuItemName(cmdNum, oldLabel);
        if (newLabel == null) {
            this.setEnabled(false);
        } else {
            this.setEnabled(true);
            if (newLabel != oldLabel) {
                this.setText(newLabel);
            }
        }
    }

    public void updateMenus() {

        CmdMenuBar.updateMenu(this);
    }
}
