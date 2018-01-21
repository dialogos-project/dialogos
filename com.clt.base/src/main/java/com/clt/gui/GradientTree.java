package com.clt.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTree;
import javax.swing.tree.TreeCellEditor;

/**
 * @author dabo
 *
 */
public class GradientTree extends JTree {

    private Color background;
    private Color gradientStart;
    private Color gradientEnd;
    private Color gradientBorder;

    public GradientTree() {
        this(new Color(231, 237, 248), new Color(173, 187, 208), new Color(152, 170, 196),
                new Color(143, 156, 181));
    }

    public GradientTree(Color background, Color gradientStart, Color gradientEnd, Color gradientBorder) {
        super();

        this.background = background;
        this.gradientStart = gradientStart;
        this.gradientEnd = gradientEnd;
        this.gradientBorder = gradientBorder;

        this.setOpaque(false);

        this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {

                GradientTree.this.repaint();
            }

            public void focusLost(FocusEvent e) {

                GradientTree.this.repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {

        int x = 0;
        int width = this.getWidth();

        g.setColor(this.background);
        g.fillRect(x, 0, width, this.getHeight());

        if (this.getSelectionCount() > 0) {
            int[] selection = this.getSelectionModel().getSelectionRows();

            boolean focused = this.hasFocus();
            if (!focused) {
                TreeCellEditor editor = this.getCellEditor();
                if (editor instanceof Component) {
                    focused = ((Component) editor).hasFocus();
                }
            }
            for (int n = 0; (n < this.getComponentCount()) && !focused; n++) {
                if (this.getComponent(n).hasFocus()) {
                    focused = true;
                }
            }

            Graphics2D gfx = (Graphics2D) g;
            // Shape clip = gfx.getClip();

            if (selection != null) {
                for (int i = 0; i < selection.length; i++) {
                    Rectangle r = this.getRowBounds(selection[i]);

                    // gfx.setClip(new Rectangle(x, r.y, width, r.height));
                    if (focused) {
                        Paint oldPaint = gfx.getPaint();
                        gfx.setPaint(new GradientPaint(0, 0, this.gradientStart, 0,
                                r.height - 1,
                                this.gradientEnd));
                        gfx.fillRect(x, r.y, width, r.height - 1);
                        gfx.setPaint(oldPaint);

                        gfx.setColor(this.gradientBorder);
                        gfx.drawLine(x, r.y + r.height - 1, width - 1, r.y + r.height - 1);
                    } else {
                        gfx.setColor(GUI.slightlyDarker(this.background));
                        gfx.fillRect(x, r.y, width, r.height);
                    }
                    // gfx.setClip(clip);
                }
            }
        }

        super.paintComponent(g);
    }
}
