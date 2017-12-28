package com.clt.diamant.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.clt.diamant.LogDocument;
import com.clt.gui.OptionPane;
import com.clt.gui.WindowUtils;
import com.clt.gui.menus.MenuCommander;
import com.clt.mac.RequiredEventHandler;

public class LogDocumentWindow
    extends SingleDocumentWindow<LogDocument> {

  public LogDocumentWindow(LogDocument d, MenuCommander superCommander,
                             RequiredEventHandler systemEventHandler) {

    super(d, superCommander, systemEventHandler, true);

    this.addWindowListener(new WindowAdapter() {

      @Override
      public void windowOpened(WindowEvent evt) {

        try {
          LogPlayerWindow playerWindow =
            new LogPlayerWindow(LogDocumentWindow.this);
          WindowUtils.setLocationRelativeTo(playerWindow,
            LogDocumentWindow.this);
          playerWindow.setVisible(true);
        }
                catch (Exception exn) {
                  exn.printStackTrace();
                  OptionPane.error(LogDocumentWindow.this, exn);
                }
              }
    });
  }


  @Override
  public boolean menuItemState(int cmd) {

    switch (cmd) {
      case cmdRun:
      case cmdDebug:
      case cmdWoz:
        return false;

      default:
        return super.menuItemState(cmd);
    }
  }

}