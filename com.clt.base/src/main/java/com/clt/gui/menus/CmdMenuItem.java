package com.clt.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.OptionPane;

public class CmdMenuItem
    extends JMenuItem {

  private MenuCommander commander;


  public CmdMenuItem(String cmdName, Runnable r) {

    this(cmdName, null, r);
  }


  public CmdMenuItem(String cmdName, int cmdKey, final Runnable r) {

    this(cmdName, 1, cmdKey, new AbstractMenuCommander() {

      @Override
      public boolean doCommand(int cmd) {

        r.run();
        return true;
      }
    }, true);
  }


  public CmdMenuItem(String cmdName, KeyStroke cmdKey, final Runnable r) {

    this(cmdName, 1, cmdKey, new AbstractMenuCommander() {

      @Override
      public boolean doCommand(int cmd) {

        r.run();
        return true;
      }
    }, true);
  }


  public CmdMenuItem(String cmdName, int cmdNum, KeyStroke cmdKey,
      MenuCommander commander) {

    this(cmdName, cmdNum, cmdKey, commander, false);
  }


  public CmdMenuItem(String cmdName, int cmdNum, int cmdKey,
      MenuCommander commander) {

    this(cmdName, cmdNum, cmdKey, commander, false);
  }


  public CmdMenuItem(String cmdName, int cmdNum, int cmdKey,
      MenuCommander commander,
                       boolean state) {

    this(cmdName, cmdNum, null, commander, state);
    if (cmdKey > 0) {
      this.setAccelerator(KeyStroke.getKeyStroke(cmdKey, this.getToolkit()
        .getMenuShortcutKeyMask()));
    }
  }


  public CmdMenuItem(String cmdName, int cmdNum, KeyStroke cmdKey,
      final MenuCommander commander,
                       boolean state) {

    super(cmdName);

    if (cmdKey != null) {
      this.setAccelerator(cmdKey);
    }

    this.commander = commander;

    this.setActionCommand(Integer.toString(cmdNum));
    this.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        try {
          if (CmdMenuItem.this.getCommandNumber() != Commands.noCmd) {
            if (!commander.doCommand(CmdMenuItem.this.getCommandNumber())) {
              CmdMenuItem.this.getToolkit().beep();
            }
          }
        }
                catch (ThreadDeath d) {
                  throw d;
                }
                catch (Throwable t) {
                  t.printStackTrace(System.err);
                  OptionPane.error(CmdMenuItem.this, new String[] {
                            GUI.getString("CouldNotComplete"), t.toString() });
                }
              }
    });
    this.setEnabled(state);
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
    this.setEnabled(this.commander.menuItemState(cmdNum));
    String oldLabel = this.getText();
    String newLabel = this.commander.menuItemName(cmdNum, oldLabel);
    if (newLabel != oldLabel) {
      this.setText(newLabel);
    }
  }
}
