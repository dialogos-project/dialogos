package com.clt.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.clt.util.Platform;

public class CmdButton
    extends JButton
    implements ActionListener {

  private Commander commander = null;

  private int cmd = 0;


  public CmdButton(Commander c, int cmd, String label) {

    super(label);
    this.commander = c;
    this.cmd = cmd;
    this.addActionListener(this);

    // work around a bug, where the _height_ of buttons is made smaller
    // when then _width_ of a button hits the minimum
    if (Platform.isMac()) {
      this.setMinimumSize(this.getPreferredSize());
    }
  }


  public CmdButton(Commander c, int cmd, String label, JPanel container,
      Object constraints) {

    this(c, cmd, label);
    container.add(this, constraints);
  }


  public CmdButton(Commander c, int cmd, String label, JPanel container) {

    this(c, cmd, label);
    container.add(this);
  }


  public CmdButton(String label, Runnable r) {

    this(r, label);
  }


  public CmdButton(final Runnable r, String label) {

    this(new Commander() {

      public boolean doCommand(int cmd) {

        if (r != null) {
          r.run();
          return true;
        }
        else {
          return false;
        }
      }
    }, 0, label);
  }


  public void setCommand(int cmd) {

    this.cmd = cmd;
  }


  public int getCommand() {

    return this.cmd;
  }


  public void actionPerformed(ActionEvent e) {

    if (this.commander != null) {
      this.commander.doCommand(this.cmd);
    }
  }
}