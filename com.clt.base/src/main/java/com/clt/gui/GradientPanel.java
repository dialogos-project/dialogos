package com.clt.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.JPanel;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class GradientPanel extends JPanel {

    private Color start, end;
    private double angle;

    /**
     * Construct a horizontal (left to right, 0 degrees) or vertical (top to
     * bottom, 270 degrees) gradient panel.
     */
    public GradientPanel(Color start, Color end, boolean horizontal) {
        this(start, end, horizontal ? 0.0 : 270.0);
    }

    /**
     * Construct a panel filled with a gradient from start to end color in the
     * given angle (in degrees). 0 degrees means left to right, 90 degrees means
     * bottom to top, 180 degrees means right to left and 270 degrees means top
     * to bottom. You may choose any value in between to construct a diagonal
     * gradient. Note that such "diagonal" angles will hold only for square
     * panels: 45 degrees always means from left-bottom to right-top which is in
     * fact 45 degrees in a square panel, but something else if the panel is
     * wider or taller.
     */
    public GradientPanel(Color start, Color end, double angle) {

        this.start = start;
        this.end = end;
        this.angle = Math.toRadians(angle);
    }


    /*
   * public boolean isOpaque() { return true; }
     */
    @Override
    protected void paintComponent(Graphics g) {

        double halfwidth = this.getWidth() / 2.0;
        double halfheight = this.getHeight() / 2.0;

        Graphics2D gfx = (Graphics2D) g;
        Paint oldPaint = gfx.getPaint();
        gfx.setPaint(new GradientPaint((float) (halfwidth * (1.0 - Math.cos(this.angle))),
                (float) (halfheight * (1.0 + Math.sin(this.angle))), this.start,
                (float) (halfwidth * (1.0 + Math.cos(this.angle))),
                (float) (halfheight * (1.0 - Math.sin(this.angle))), this.end));

        gfx.fillRect(0, 0, this.getWidth(), this.getHeight());
        gfx.setPaint(oldPaint);
    }
}
