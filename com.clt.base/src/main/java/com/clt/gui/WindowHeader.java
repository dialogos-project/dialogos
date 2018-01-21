package com.clt.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;

public class WindowHeader extends JLabel {
    public WindowHeader(String content, int align) {
        super(content);
        this.setHorizontalAlignment(align);
        this.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Dimension d = this.getSize();
        Color oldColor = g.getColor();

        // Im Windows L&F sieht die helle Linie sch... aus,
        // in allen anderen toll
        if (!UIManager.getLookAndFeel().getID().equals("Windows")) {
            g.setColor(this.getBackground().brighter());
            g.drawLine(0, 0, d.width - 1, 0);
        }

        g.setColor(this.getBackground().darker());
        g.drawLine(0, d.height - 2, d.width - 1, d.height - 2);

        g.setColor(Color.black);
        g.drawLine(0, d.height - 1, d.width - 1, d.height - 1);

        g.setColor(oldColor);
    }

}
