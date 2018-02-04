package com.clt.diamant.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.clt.diamant.Resources;
import com.clt.gui.CmdButton;
import com.clt.gui.Commander;
import com.clt.gui.Commands;
import com.clt.gui.GUI;
import com.clt.gui.Images;
import com.clt.gui.WindowUtils;

public class AlignmentDialog extends JDialog implements Commander, Commands {

    private int result = -1;
    private ButtonGroup bg = new ButtonGroup();

    public static int chooseAlignment(Component parent) {

        AlignmentDialog d = new AlignmentDialog(parent);
        d.setVisible(true);
        return d.result;
    }

    private AlignmentDialog(Component parent) {

        super(GUI.getFrameForComponent(parent), Resources.getString("Alignment"),
                true);
        this.setResizable(false);

        Container content = this.getContentPane();
        content.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = gbc.gridy = 0;
        gbc.gridwidth = 2;

        gbc.insets = new Insets(6, 12, 6, 12);
        content.add(new JLabel(Resources.getString("ChooseAlignment")), gbc);

        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 24, 0, 0);

        gbc.gridy = 1;
        gbc.gridx = 0;
        content.add(this.createRadio(Resources.getString("Left"),
                "align/HoriLeft.gif", 0), gbc);
        gbc.gridy++;
        content.add(this.createRadio(Resources.getString("Center"),
                "align/HoriMid.gif", 1), gbc);
        gbc.gridy++;
        content.add(this.createRadio(Resources.getString("Right"),
                "align/HoriRight.gif", 2), gbc);

        gbc.insets = new Insets(6, 24, 0, 24);
        gbc.gridy = 1;
        gbc.gridx = 1;
        content.add(this.createRadio(Resources.getString("Top"),
                "align/VertTop.gif", 4), gbc);
        gbc.gridy++;
        content.add(this.createRadio(Resources.getString("Center"),
                "align/VertMid.gif", 5), gbc);
        gbc.gridy++;
        content.add(this.createRadio(Resources.getString("Bottom"),
                "align/VertBot.gif", 6), gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JPanel buttons = new JPanel(new GridLayout(1, 2, 10, 0));

        JButton ok = new CmdButton(this, Commands.cmdOK, Resources.getString("OK")), cancel
                = new CmdButton(this, Commands.cmdCancel, Resources.getString("Cancel"));
        buttons.add(cancel);
        buttons.add(ok);

        this.getRootPane().setDefaultButton(ok);

        gbc.insets = new Insets(24, 12, 12, 12);
        content.add(buttons, gbc);

        this.pack();
        WindowUtils.setLocationRelativeTo(this, GUI.getWindowForComponent(parent));
    }

    private JPanel createRadio(String title, String icon, final int cmd) {

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

        final JRadioButton b = new JRadioButton();
        this.bg.add(b);
        b.addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {

                if (b.isSelected()) {
                    AlignmentDialog.this.result = cmd;
                }
            }
        });

        p.add(b);

        ImageIcon im = null;
        try {
            im = Images.load(icon);
        } catch (Exception ignore) {
        }

        JLabel l = new JLabel(title, im, SwingConstants.LEFT);
        l.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {

                b.setSelected(true);
            }
        });
        p.add(l);

        return p;
    }

    public boolean doCommand(int cmd) {

        switch (cmd) {
            case cmdOK:
                this.dispose();
                break;

            case cmdCancel:
                this.result = -1;
                this.dispose();
                break;

            default:
                return false;
        }
        return true;
    }
}
