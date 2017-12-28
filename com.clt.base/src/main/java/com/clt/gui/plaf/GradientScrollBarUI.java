/*
 * @(#)GradientScrollBarUI.java
 * Created on 26.05.2006 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui.plaf;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;

/**
 * @author dabo
 * 
 */
public class GradientScrollBarUI
    extends javax.swing.plaf.basic.BasicScrollBarUI {

  private int scrollBarWidth = 13;
  private int scrollBarInset = 4;

  private Color thumbColor;

  @SuppressWarnings("unused")
  private Color arrowColor;


  public GradientScrollBarUI() {

    this(Color.BLACK, Color.RED);
  }


  public GradientScrollBarUI(Color thumbColor, Color arrowColor) {

    super();

    this.thumbColor = thumbColor;
    this.arrowColor = arrowColor;
  }


  @Override
  public void paint(Graphics g, JComponent c) {

    this.paintTrack(g, c, this.getTrackBounds());
    Rectangle thumbBounds = this.getThumbBounds();
    if (thumbBounds.intersects(g.getClipBounds())) {
      this.paintThumb(g, c, thumbBounds);
    }
  }


  @Override
  protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {

    this.paintGradient(g, thumbBounds, -this.scrollBarInset,
      -this.scrollBarInset,
            this.scrollbar.getOrientation() == Adjustable.HORIZONTAL, false);
  }


  @Override
  protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {

  }


  @Override
  protected JButton createDecreaseButton(int orientation) {

    return new GradientButton(orientation);
  }


  @Override
  protected JButton createIncreaseButton(int orientation) {

    return new GradientButton(orientation);
  }


  @Override
  public Dimension getPreferredSize(JComponent c) {

    if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
      return new Dimension(100, this.scrollBarWidth);
    }
    else {
      return new Dimension(this.scrollBarWidth, 100);
    }
  }


  @Override
  protected Dimension getMinimumThumbSize() {

    if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
      return new Dimension(20, this.scrollBarWidth);
    }
    else {
      return new Dimension(this.scrollBarWidth, 20);
    }
  }


  @Override
  protected Dimension getMaximumThumbSize() {

    if (this.scrollbar.getOrientation() == Adjustable.HORIZONTAL) {
      return new Dimension(Integer.MAX_VALUE, this.scrollBarWidth);
    }
    else {
      return new Dimension(this.scrollBarWidth, Integer.MAX_VALUE);
    }
  }


  private static int curve(int shift, int i, int max) {

    if (shift >= 0) {
      return (int)Math.round(Math.sin(Math.PI * i / (max - 1)) * shift);
    }
    else {
      return -shift
        - (int)Math.round(Math.sin(Math.PI * i / (max - 1)) * -shift);
    }
  }


  private static int brighten(int orig, int index, int mid) {

    if (index < mid) {
      return Math.min(255, orig + 30 + 15 * (mid - index));
    }
    else if (index > mid + 1) {
      return Math.min(255, orig + 10 + 5 * (index - (mid + 1)));
    }
    else {
      return orig;
    }
  }


  private void paintGradient(Graphics g, Rectangle r, int left_shift,
      int right_shift,
            boolean horizontal, boolean reverse) {

    g.translate(r.x, r.y);

    int width = r.width;
    int height = r.height;

    if (horizontal) {
      int mid = height / 2;

      for (int y = 0; y < height; y++) {
        int left_margin = GradientScrollBarUI.curve(left_shift, y, height);
        int right_margin = GradientScrollBarUI.curve(right_shift, y, height);
        if (reverse) {
          int x = left_margin;
          left_margin = right_margin;
          right_margin = x;
        }
        g.setColor(this.thumbColor);
        g.drawLine(left_margin, y, left_margin, y);
        g.drawLine(width - 1 - right_margin, y, width - 1 - right_margin, y);
        Color c;
        if ((y == 0) || (y == height - 1)) {
          c = this.thumbColor;
        }
        else {
          c =
            new Color(GradientScrollBarUI.brighten(this.thumbColor.getRed(), y,
              mid), GradientScrollBarUI.brighten(
                      this.thumbColor.getGreen(), y, mid), GradientScrollBarUI
              .brighten(this.thumbColor.getBlue(), y, mid),
                      this.thumbColor.getAlpha());
        }
        g.setColor(c);
        g.drawLine(left_margin + 1, y, (width - 1) - right_margin - 1, y);
      }
    }
    else {
      int mid = width / 2;

      for (int x = 0; x < width; x++) {
        int left_margin = GradientScrollBarUI.curve(left_shift, x, width);
        int right_margin = GradientScrollBarUI.curve(right_shift, x, width);
        if (reverse) {
          int y = left_margin;
          left_margin = right_margin;
          right_margin = y;
        }
        g.setColor(this.thumbColor);
        g.drawLine(x, left_margin, x, left_margin);
        g.drawLine(x, height - 1 - right_margin, x, height - 1 - right_margin);
        Color c;
        if ((x == 0) || (x == width - 1)) {
          c = this.thumbColor;
        }
        else {
          c =
            new Color(GradientScrollBarUI.brighten(this.thumbColor.getRed(), x,
              mid), GradientScrollBarUI.brighten(
                      this.thumbColor.getGreen(), x, mid), GradientScrollBarUI
              .brighten(this.thumbColor.getBlue(), x, mid),
                      this.thumbColor.getAlpha());
        }
        g.setColor(c);
        g.drawLine(x, 1, x, (height - 1) - right_margin - 1);
      }
    }

    g.translate(-r.x, -r.y);
  }

  private class GradientButton
        extends JButton {

    private int orientation;


    public GradientButton(int orientation) {

      this.orientation = orientation;

      this.setFocusPainted(false);
      this.setMargin(new Insets(0, 0, 0, 0));
      this.setBorderPainted(false);
    }


    @Override
    public void paint(Graphics g) {

      boolean horizontal;
      switch (this.orientation) {
        case SwingConstants.EAST:
        case SwingConstants.WEST:
          horizontal = true;
          break;
        case SwingConstants.SOUTH:
        case SwingConstants.NORTH:
          horizontal = false;
          break;
        default:
          throw new IllegalArgumentException("illegal orientation");
      }

      int right_shift = -GradientScrollBarUI.this.scrollBarInset;
      int left_shift = GradientScrollBarUI.this.scrollBarInset;

      GradientScrollBarUI.this.paintGradient(g, this.getBounds(), left_shift,
        right_shift, horizontal,
                (this.orientation == SwingConstants.WEST)
                  || (this.orientation == SwingConstants.SOUTH));
    }


    @Override
    public Dimension getPreferredSize() {

      if ((this.orientation == SwingConstants.EAST)
        || (this.orientation == SwingConstants.WEST)) {
        return new Dimension(18, GradientScrollBarUI.this.scrollBarWidth);
      }
      else {
        return new Dimension(GradientScrollBarUI.this.scrollBarWidth, 18);
      }
    }
  }
}
