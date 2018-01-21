package com.clt.gui.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.Border;

/**
 * Draw an arbitrary combination of lines on the border. The lines are given in
 * form of a string, with the following encoding:
 * <ul>
 * <li>l = Draw a thin line on the left
 * <li>L = Draw a bold line on the left
 * <li>r = Draw a thin line on the right
 * <li>R = Draw a bold line on the right
 * <li>t = Draw a thin line on the top
 * <li>T = Draw a bold line on the top
 * <li>b = Draw a thin line on the bottom
 * <li>B = Draw a bold line on the bottom
 * </ul>
 * Per default, "thin" means a one pixel line and "bold" means a two pixel line.
 *
 * @author dabo
 */
public class LinesBorder implements Border {

    private Color color;

    private int left, right, top, bottom;

    /**
     * Construct a black border using the given line encoding.
     */
    public LinesBorder(String lines) {

        this(lines, Color.black);
    }

    /**
     * Construct a border using the given line encoding and color.
     */
    public LinesBorder(String lines, Color c) {

        this(lines, c, 1, 3);
    }

    /**
     * Construct a black border using the given line encoding. Thin and bold
     * lines will be drawn using the respective number of pixels.
     */
    public LinesBorder(String lines, int thinLine, int boldLine) {

        this(lines, Color.black, thinLine, boldLine);
    }

    /**
     * Construct a border using the given line encoding and color. Thin and bold
     * lines will be drawn using the respective number of pixels.
     */
    public LinesBorder(String lines, Color c, int thinLine, int boldLine) {

        this.color = c;
        this.left
                = lines.indexOf('L') >= 0 ? boldLine : (lines.indexOf('l') >= 0 ? thinLine
                : 0);
        this.right
                = lines.indexOf('R') >= 0 ? boldLine : (lines.indexOf('r') >= 0 ? thinLine
                : 0);
        this.top
                = lines.indexOf('T') >= 0 ? boldLine : (lines.indexOf('t') >= 0 ? thinLine
                : 0);
        this.bottom
                = lines.indexOf('B') >= 0 ? boldLine : (lines.indexOf('b') >= 0 ? thinLine
                : 0);
    }

    public boolean isBorderOpaque() {

        // opaque only if the color isn't transparent
        return this.color.getAlpha() == 255;
    }

    public Insets getBorderInsets(Component c) {

        return new Insets(this.top, this.left, this.bottom, this.right);
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width,
            int height) {

        g.setColor(this.color);

        // draw left and right border
        g.fillRect(x, y, this.left, height);
        g.fillRect(x + width - this.right, y, this.right, height);

        // draw top and bottom border
        // since the color might be transparent, avoid drawing a pixel twice
        g.fillRect(x + this.left, y, width - this.left - this.right, this.top);
        g.fillRect(x + this.left, y + height - this.bottom, width - this.left
                - this.right, this.bottom);
    }
}
