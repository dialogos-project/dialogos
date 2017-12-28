package com.clt.diamant.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.UIManager;

public class AnchorChooser
    extends JComponent {

  int anchor;
  int h_diff = 0;
  int v_diff = 0;

  public static final int TOP_LEFT = 0, TOP = 1, TOP_RIGHT = 2, LEFT = 3,
      CENTER = 4, RIGHT = 5,
            LEFT_BOTTOM = 6, BOTTOM = 7, RIGHT_BOTTOM = 8;


  public AnchorChooser() {

    this(AnchorChooser.CENTER);
  }


  public AnchorChooser(int anchor) {

    this.setAnchor(anchor);
    this.addMouseListener(new MouseAdapter() {

      int tile;


      @Override
      public void mousePressed(MouseEvent e) {

        this.tile = this.getTileAt(e.getX(), e.getY());
      }


      @Override
      public void mouseReleased(MouseEvent e) {

        if (AnchorChooser.this.isEnabled()) {
          int t = this.getTileAt(e.getX(), e.getY());
          if ((t == this.tile) && (t != -1)) {
            AnchorChooser.this.setAnchor(this.tile);
            AnchorChooser.this.repaint();
          }
        }
      }


      private int getTileAt(int x, int y) {

        int totalsize =
          Math.min(AnchorChooser.this.getWidth(), AnchorChooser.this
            .getHeight());
        if ((x < 0) || (y < 0) || (x >= totalsize) || (y >= totalsize)) {
          return -1;
        }
        else {
          int size = (totalsize - 1) / 3;
          return (y / size) * 3 + (x / size);
        }
      }
    });
  }


  public void setAnchor(int anchor) {

    this.anchor = anchor;
  }


  public int getAnchor() {

    return this.anchor;
  }


  public void setShrink(int h_diff, int v_diff) {

    this.h_diff = AnchorChooser.signum(h_diff);
    this.v_diff = AnchorChooser.signum(v_diff);
    this.repaint();
  }


  @Override
  protected void paintComponent(Graphics g) {

    int totalsize = Math.min(this.getWidth(), this.getHeight());

    int size = (totalsize - 1) / 3;
    int x, y;

    g.setColor(this.getBackground());
    g.fillRect(0, 0, this.getWidth(), this.getHeight());

    g.setColor(Color.black);
    g.drawRect(0, 0, size * 3, size * 3);
    for (x = 1; x < 3; x++) {
      g.drawLine(x * size, 0, x * size, 3 * size);
      g.drawLine(0, x * size, 3 * size, x * size);
    }

    for (x = 0; x < 3; x++) {
      for (y = 0; y < 3; y++) {
        Color body, top, bot;
        if (this.anchor == (3 * y + x)) {
          Object o = UIManager.get("Button.select");
          body = o instanceof Color ? (Color)o : new Color(136, 136, 136);
          top = new Color(85, 85, 85);
          bot = body;
        }
        else {
          Object o;
          if (this.isEnabled()) {
            o = UIManager.get("Button.background");
          }
          else {
            o = UIManager.get("Button.disabled");
          }
          body = o instanceof Color ? (Color)o : new Color(221, 221, 221);
          top = Color.white;
          bot = new Color(136, 136, 136);
        }
        g.setColor(body);
        g.fillRect(x * size + 2, y * size + 2, size - 3, size - 3);
        g.setColor(top);
        g.drawLine(x * size + 1, y * size + size - 1, x * size + 1, y * size
          + 1);
        g.drawLine(x * size + 2, y * size + 1, x * size + size - 1, y * size
          + 1);
        g.setColor(bot);
        g.drawLine(x * size + 2, y * size + size - 1, x * size + size - 1, y
          * size + size
                      - 1);
        g.drawLine(x * size + size - 1, y * size + size - 2, x * size + size
          - 1, y * size
                      + 2);
      }
    }

    g.setColor(Color.darkGray);
    x = this.anchor % 3;
    y = this.anchor / 3;
    int offset = 3;
    int doffset = Math.round(offset / 0.8f);
    int length = 10;

    if (x < 2) {
      this.fillArrow(g, (x + 1) * size - offset, y * size + size / 2, length,
        0, this.h_diff);
      if (y > 0) {
        this.fillArrow(g, (x + 1) * size - doffset, y * size + doffset, length,
          -length, this.h_diff
                      + this.v_diff);
      }
      if (y < 2) {
        this.fillArrow(g, (x + 1) * size - doffset, (y + 1) * size - doffset,
          length, length,
                  this.h_diff + this.v_diff);
      }
    }
    if (x > 0) {
      this.fillArrow(g, x * size + offset, y * size + size / 2, -length, 0,
        this.h_diff);
      if (y > 0) {
        this.fillArrow(g, x * size + doffset, y * size + doffset, -length,
          -length, this.h_diff
                      + this.v_diff);
      }
      if (y < 2) {
        this.fillArrow(g, x * size + doffset, (y + 1) * size - doffset,
          -length, length, this.h_diff
                      + this.v_diff);
      }
    }
    if (y > 0) {
      this.fillArrow(g, x * size + size / 2, y * size + offset, 0, -length,
        this.v_diff);
    }
    if (y < 2) {
      this.fillArrow(g, x * size + size / 2, (y + 1) * size - offset, 0,
        length, this.v_diff);
    }
  }


  private void fillArrow(Graphics g, int x, int y, int ox, int oy, int direction) {

    /*
     * if (direction < 0) fillArrow2(g, x+ox, y+oy, -ox, -oy, true); else
     * fillArrow2(g, x, y, ox, oy, direction > 0);
     */
    this.fillArrow2(g, x, y, ox, oy, true);
  }


  private void fillArrow2(Graphics g, int x, int y, int ox, int oy,
      boolean arrow) {

    if (Math.abs(ox) == Math.abs(oy)) {
      ox = Math.round(ox * 0.9f);
      oy = Math.round(oy * 0.9f);
    }

    g.drawLine(x, y, x + ox, y + oy);

    if (arrow) {
      x = x + ox;
      y = y + oy;
      ox = AnchorChooser.signum(ox);
      oy = AnchorChooser.signum(oy);
      int x2 = x + 2 * ox;
      int y2 = y + 2 * oy;

      if (ox == 0) {
        g.drawLine(x, y2, x, y2);
        g.drawLine(x - 1, y + oy, x + 1, y + oy);
        g.drawLine(x - 2, y, x + 2, y);
      }
      else if (oy == 0) {
        g.drawLine(x2, y, x2, y);
        g.drawLine(x + ox, y - 1, x + ox, y + 1);
        g.drawLine(x, y - 2, x, y + 2);
      }
      else {
        g.drawLine(x2, y2, x2, y2);
        g.drawLine(x + ox, y2, x2, y + oy);
        g.drawLine(x, y2, x2, y);
        g.drawLine(x - ox, y2, x2, y - oy);
      }
    }
  }


  private static int signum(int x) {

    return x == 0 ? 0 : (x > 0 ? 1 : -1);
  }


  @Override
  public boolean isOpaque() {

    return true;
  }


  @Override
  public Dimension getPreferredSize() {

    // return new Dimension(73, 73);
    return new Dimension(67, 67);
  }
}