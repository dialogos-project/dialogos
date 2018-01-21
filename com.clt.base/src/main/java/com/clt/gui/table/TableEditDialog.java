package com.clt.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import com.clt.gui.CmdButton;
import com.clt.gui.Commander;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.WindowUtils;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class TableEditDialog<E> extends JDialog implements Commander, Commands {

    private TableEditor<E> table;

    public TableEditDialog(Component parent, final ItemTableModel<E> model,
            String title,
            String info, boolean includeEditButton) {

        super(GUI.getFrameForComponent(parent), title, true);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        // setResizable(false);

        Container c = this.getContentPane();
        c.setLayout(new BorderLayout());

        JButton okButton = new CmdButton(this, Commands.cmdOK, GUI.getString("OK"));

        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {

                TableEditDialog.this.doCommand(Commands.cmdOK);
            }
        });

        this.table = new TableEditor<E>(model, info, includeEditButton,
                        new JButton[]{okButton});
        c.add(this.table, BorderLayout.CENTER);

        GUI.setDefaultButtons(this, okButton, null);
        GUI.assignMnemonics(this.getContentPane());

        this.pack();
        WindowUtils.setLocationRelativeTo(this, GUI.getWindowForComponent(parent));
    }

    public boolean doCommand(int cmd) {

        switch (cmd) {
            case cmdOK:
                if (this.table.readyToClose()) {
                    this.dispose();
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
