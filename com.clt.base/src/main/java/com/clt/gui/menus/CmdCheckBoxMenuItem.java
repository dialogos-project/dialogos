package com.clt.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;

import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class CmdCheckBoxMenuItem extends JCheckBoxMenuItem {

    MenuCommander commander;

    public CmdCheckBoxMenuItem(String cmdName, int cmdNum, KeyStroke cmdKey, MenuCommander commander) {
        this(cmdName, cmdNum, cmdKey, commander, false);
    }

    public CmdCheckBoxMenuItem(String cmdName, int cmdNum, int cmdKey, MenuCommander commander) {
        this(cmdName, cmdNum, cmdKey, commander, false);
    }

    public CmdCheckBoxMenuItem(String cmdName, int cmdNum, int cmdKey, MenuCommander commander, boolean state) {
        this(cmdName, cmdNum, null, commander, state);
        if (cmdKey > 0) {
            this.setAccelerator(KeyStroke.getKeyStroke(cmdKey, this.getToolkit().getMenuShortcutKeyMask()));
        }
    }

    public CmdCheckBoxMenuItem(String cmdName, int cmdNum, KeyStroke cmdKey, final MenuCommander commander, boolean state) {
        super(cmdName);

        this.commander = commander;

        if (cmdKey != null) {
            this.setAccelerator(cmdKey);
        }

        this.setActionCommand(Integer.toString(cmdNum));
        this.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                try {
                    if (CmdCheckBoxMenuItem.this.getCommandNumber() != Commands.noCmd) {
                        if (!commander.doCommand(CmdCheckBoxMenuItem.this
                                .getCommandNumber())) {
                            CmdCheckBoxMenuItem.this.getToolkit().beep();
                        }
                    }
                } catch (Throwable t) {
                    OptionPane.error(CmdCheckBoxMenuItem.this, new String[]{
                        GUI.getString("CouldNotComplete"), t.toString()});
                }
            }
        });
        this.setEnabled(true);
        this.setSelected(state);
    }

    public int getCommandNumber() {
        try {
            return Integer.parseInt(this.getActionCommand());
        } catch (NumberFormatException exn) {
            return Commands.noCmd;
        }
    }

    public void update() {
        int cmdNum = this.getCommandNumber();
        this.setSelected(this.commander.menuItemState(cmdNum));
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
}
