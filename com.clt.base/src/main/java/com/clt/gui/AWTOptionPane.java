package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.clt.resources.DynamicResourceBundle;

/**
 * This class is useful for putting up a dialog if Swing is not available.
 *
 */
public class AWTOptionPane {

    private static DynamicResourceBundle resources = new DynamicResourceBundle("com.clt.gui.Resources");

    public static void error(Frame parent, String message) {
        final Dialog dialog = new Dialog(parent != null ? parent : new Frame(), AWTOptionPane.resources.getString("Error"), true);
        dialog.setResizable(false);

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dialog.dispose();
            }
        });

        int lines = 1;
        int start = -1;
        while ((start = message.indexOf('\n', start + 1)) >= 0) {
            lines++;
        }

        dialog.setLayout(new BorderLayout(20, 20));

        start = 0;
        int end;

        Panel p1 = new Panel(new GridLayout(lines, 1));
        do {
            end = message.indexOf('\n', start);
            if (end <= 0) {
                end = message.length();
            }
            String s = message.substring(start, end);
            Label line = new Label(s);
            p1.add(line);
            start = end + 1;
        } while (end < message.length());

        Button b = new Button("OK");
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        b.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    dialog.dispose();
                }
            }
        });

        Panel p2 = new Panel();
        p2.add(b);

        dialog.add(p1, BorderLayout.CENTER);
        dialog.add(p2, BorderLayout.SOUTH);

        dialog.pack();

        Dimension screensize = dialog.getToolkit().getScreenSize();
        Dimension size = dialog.getSize();

        dialog.setLocation((screensize.width - size.width) / 2, (screensize.height - size.height) / 2);
        dialog.setVisible(true);
    }
}
