/*
 * @(#)ListEditorDialog.java
 * Created on Thu Jul 01 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class ListEditorDialog
    extends JDialog
    implements Commander {

  public ListEditorDialog(Component parent, String name,
      final ListEditor.Model model) {

    super(GUI.getFrameForComponent(parent), name, true);
    //this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    JButton okButton = new CmdButton(this, Commands.cmdOK, GUI.getString("OK"));

    JPanel p = new JPanel(new BorderLayout());
    p.add(new ListEditor(model, true, okButton));
    this.setContentPane(p);

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosing(WindowEvent e) {

        ListEditorDialog.this.doCommand(Commands.cmdOK);
      }
    });

    GUI.setDefaultButtons(this, okButton, null);
    this.pack();

    WindowUtils.setLocationRelativeTo(this, this.getParent());
  }


  public boolean doCommand(int cmd) {

    switch (cmd) {
      case cmdOK:
        this.dispose();
        break;
      default:
        return false;
    }
    return true;
  }
}