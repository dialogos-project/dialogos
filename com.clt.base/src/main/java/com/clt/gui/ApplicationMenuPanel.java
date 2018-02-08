package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.clt.gui.border.LinesBorder;

/**
 * @author dabo
 *
 */
public class ApplicationMenuPanel extends JPanel {

    public ApplicationMenuPanel(String appName, Image image, Action[] menuItems, Action[] buttons) {
        this.setLayout(new BorderLayout());

        JPanel main = new JPanel(new GridLayout(1, 0));
        main.setOpaque(true);
        main.setBackground(Color.WHITE);
        JPanel im = new JPanel(new FlowLayout(FlowLayout.CENTER));
        im.setOpaque(false);
        im.setBorder(BorderFactory.createEmptyBorder(36, 24, 36, 24));
        if (image != null) {
            im.add(new JLabel(new ImageIcon(image)));
        }

        main.add(im);

        JPanel menu = new JPanel(new GridBagLayout());
        menu.setOpaque(false);
        menu.setBorder(BorderFactory.createEmptyBorder(36, 12, 36, 48));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6, 6, 15, 6);
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel logo = new JLabel(new ImageIcon(ClassLoader.getSystemResource("com/clt/resources/MiniUdS.png")));
        logo.setHorizontalAlignment(SwingConstants.LEFT);
        menu.add(logo, gbc);

        gbc.gridy++;
        JLabel title = new JLabel(appName);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28.0f));
        menu.add(title, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);

        for (int i = 0; i < menuItems.length; i++) {
            gbc.weightx = 0.0;
            AbstractButton b = new JButton();
            b.setMargin(new Insets(6, 6, 6, 6));
            b.setOpaque(false);
            b.setIcon((Icon) menuItems[i].getValue(Action.SMALL_ICON));
            b.addActionListener(menuItems[i]);
            menu.add(b, gbc);
            gbc.gridx++;
            gbc.weightx = 1.0;
            JLabel label = new JLabel((String) menuItems[i].getValue(Action.NAME));
            menu.add(label, gbc);
            gbc.gridx--;
            gbc.gridy++;
        }

        main.add(menu);

        this.add(main, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(true);
        footer.setBackground(new Color(224, 224, 224));

        JPanel buttonBar = new JPanel(new GridLayout(1, 0, 12, 12));
        buttonBar.setOpaque(false);
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                JButton b = new JButton((String) buttons[i].getValue(Action.NAME));
                b.setOpaque(false);
                b.addActionListener(buttons[i]);
                buttonBar.add(b);
            }
            buttonBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        } else {
            buttonBar.setBorder(BorderFactory.createEmptyBorder(12, 12, 30, 12));
        }

        footer.setBorder(new LinesBorder("t", Color.GRAY));
        footer.add(buttonBar, BorderLayout.EAST);

        this.add(footer, BorderLayout.SOUTH);
    }
}
