package com.clt.gui.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A frame border is a one-pixel line that automatically adjusts to the
 * background color of its component, drawing a slightly brighter line at the
 * left and top side, and a slightly darker line at the bottom and right side of
 * the component, giving the component a bevel that reflects the color of the
 * component.
 * 
 * @author dabo
 * 
 */
public class FrameBorder
    extends AbstractBorder {

  public enum Type {
        SQUARE,
        ROUND
  };

  private Type type;


  public FrameBorder() {

    this(Type.SQUARE);
  }


  public FrameBorder(Type type) {

    this.type = type;
  }


  protected Color getForegroundColor(Component c) {

    return c.getForeground();
  }


  protected Color getBackgroundColor(Component c) {

    return c.getBackground();
  }


  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height) {

    Color saveColor = g.getColor();

    Color co = this.getBackgroundColor(c);

    int top = y, bottom = y + height - 1, left = x, right = x + width - 1;

    // Der Rahmen uebernimmt die Transparenz des Hintergrunds. Deshalb ist
    // es wichtig, dass jeder Pixel des Rahmens genau einmal gezeichnet
    // wird und nicht mehrere Linien oder Rechtecke uebereinander

    int inner_offset = 0;
    // aeusserer Rahmen
    g.setColor(this.getForegroundColor(c));
    if (this.type == Type.ROUND) {
      g.drawRoundRect(left, top, right - left, bottom - top, 8, 8);
      inner_offset = 3;
      // die inneren Ecken sind durch die Rundung erschlagen
    }
    else {
      g.drawRect(left, top, right - left, bottom - top);
      // Innere Ecken

      // unten links und oben rechts in Originalfarbe
      g.setColor(co);
      g.drawLine(left + 1, bottom - 1, left + 1, bottom - 1);
      g.drawLine(right - 1, top + 1, right - 1, top + 1);

      // unten rechts etwas dunkler
      g.setColor(FrameBorder.copyAlpha(co, co.darker()));
      g.drawLine(right - 1, bottom - 1, right - 1, bottom - 1);

      // oben links etwas heller
      g.setColor(FrameBorder.copyAlpha(co, co.brighter()));
      g.drawLine(left + 1, top + 1, left + 1, top + 1);

      inner_offset = 2;
    }

    // innerer Rahmen
    // unten und rechts ein bisschen dunkler
    g.setColor(FrameBorder.copyAlpha(co, co.darker()));
    g.drawLine(right - 1, top + inner_offset, right - 1, bottom - inner_offset); // rechts
    g.drawLine(left + inner_offset, bottom - 1, right - inner_offset,
      bottom - 1); // unten

    // oben und links ein bisschen heller
    g.setColor(FrameBorder.copyAlpha(co, co.brighter()));
    g.drawLine(left + 1, top + inner_offset, left + 1, bottom - inner_offset); // links
    g.drawLine(left + inner_offset, top + 1, right - inner_offset, top + 1); // oben

    /*
     * // aeusserer Rahmen g.setColor(c.getForeground()); g.drawRect(x, y,
     * width-1, height-1); //innerer Rahmen g.setColor(co); g.drawRect(x+1, y+1,
     * width-3, height-3); // die Ecken unten links und oben rechts sind in der
     * Originalfarbe g.drawLine(x+1, y+height-2, x+1, y+height-2);
     * g.drawLine(x+width-2, y+1, x+width-2, y+1); // unten und rechts ein
     * bisschen dunkler g.setColor(copyAlpha(co, co.darker())); g.drawLine(x+2,
     * y+height-2, x+width-2, y+height-2); g.drawLine(x+width-2, y+height-3,
     * x+width-2, y+2); // oben und links ein bisschen heller
     * g.setColor(copyAlpha(co, co.brighter())); g.drawLine(x+1, y+height-3,
     * x+1, y+1); g.drawLine(x+2, y+1, x+width-3, y+1);
     */

    g.setColor(saveColor);
  }


  private static Color copyAlpha(Color src, Color dst) {

    int alpha = src.getAlpha();
    return new Color(dst.getRed(), dst.getGreen(), dst.getBlue(), alpha);
  }


  @Override
  public Insets getBorderInsets(Component c) {

    return new Insets(2, 2, 2, 2);
  }


  @Override
  public Insets getBorderInsets(Component c, Insets insets) {

    insets.left = insets.top = insets.right = insets.bottom = 2;
    return insets;
  }


  @Override
  public boolean isBorderOpaque() {

    // false, since we are possibly transparent
    return false;
  }

}