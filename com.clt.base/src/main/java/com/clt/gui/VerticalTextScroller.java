package com.clt.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.clt.util.StringTools;

public class VerticalTextScroller
    extends JComponent
    implements SwingConstants {

  final static int hBorder = 10;

  private int scrollOffset = 0;
  private int maxLineWidth = 100;
  private int lineHeight = 14;
  private int ascent = 12;

  private Line[] lines;

  private int align = SwingConstants.RIGHT;

  private Color flashColor = null;

  private boolean initialized = false;

  private Timer timer;


  public VerticalTextScroller() {

    this(40);
  }


  public VerticalTextScroller(int delay) {

    this.setDoubleBuffered(true);
    this.lines = new Line[0];
    this.timer = new Timer(delay, new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        VerticalTextScroller.this.scrollOffset++;
        if (VerticalTextScroller.this.scrollOffset >= (VerticalTextScroller.this.lineHeight * VerticalTextScroller.this.lines.length)
          + VerticalTextScroller.this.getHeight()) {
          VerticalTextScroller.this.scrollOffset = 0;
        }
        VerticalTextScroller.this.repaint();
      }
    });
    this.timer.setInitialDelay(1000);
  }


  public void setHorizontalAlignment(int align) {

    if ((align != SwingConstants.LEFT) && (align != SwingConstants.CENTER)
      && (align != SwingConstants.RIGHT)) {
      throw new IllegalArgumentException("Illegal horizontal alignment: "
        + align);
    }
    this.align = align;
  }


  public void setFlashColor(Color c) {

    this.flashColor = c;
  }


  public synchronized void setText(String[] text) {

    this.setText(text, GUI.getSmallSystemFont());
  }


  public synchronized void setText(String[] text, Font font) {

    Font[] fonts = new Font[text.length];
    for (int i = 0; i < text.length; i++) {
      fonts[i] = font;
    }
    this.setText(text, fonts);
  }


  public synchronized void setText(String[] text, String fontname, int size,
      int styles[]) {

    Font[] fonts = new Font[text.length];
    for (int i = 0; i < text.length; i++) {
      fonts[i] = new Font(fontname, styles[i], size);
    }
    this.setText(text, fonts);
  }


  public synchronized void setText(String[] text, Font[] fonts) {

    this.lines = new Line[text.length];

    for (int i = 0; i < text.length; i++) {
      this.lines[i] = new Line(text[i], fonts[i]);
    }

    this.initialized = false;
  }


  public synchronized void setText(String text) {

    this.setText(text, GUI.getSmallSystemFont());
  }


  public synchronized void setText(String text, Font font) {

    String[] s = StringTools.split(text, '\n');
    this.lines = new Line[s.length];
    for (int i = 0; i < s.length; i++) {
      if (s[i].startsWith("<")) {
        int attributes = Font.PLAIN;
        int size = font.getSize();
        int j = 1;
        while (s[i].charAt(j) != '>') {
          switch (s[i].charAt(j)) {
            case 'b':
            case 'B':
              attributes |= Font.BOLD;
              break;
            case 'i':
            case 'I':
              attributes |= Font.ITALIC;
              break;
            case 's':
            case 'S':
              boolean neg;
              if (s[i].charAt(j + 1) == '-') {
                neg = true;
                j++;
              }
              else if (s[i].charAt(j + 1) == '+') {
                neg = false;
                j++;
              }
              else {
                neg = false;
                size = 0;
              }
              int n = 0;
              while (Character.isDigit(s[i].charAt(j + 1))) {
                n *= 10;
                n += Character.digit(s[i].charAt(j + 1), 10);
                j++;
              }
              if (neg) {
                size -= n;
              }
              else {
                size += n;
              }
              break;
          }
          j++;
        }
        this.lines[i] =
          new Line(s[i].substring(j + 1), new Font(font.getName(), attributes,
                    size));
      }
      else {
        this.lines[i] = new Line(s[i], font);
      }
    }

    this.initialized = false;
  }


  public void start() {

    this.timer.start();
  }


  @Override
  public void removeNotify() {

    this.timer.stop();
    super.removeNotify();
  }


  private synchronized void calcFontValues() {

    Graphics g = this.getGraphics();
    if (g == null) {
      return;
    }

    this.maxLineWidth = 0;
    this.lineHeight = 0;
    this.ascent = 0;
    for (int i = 0; i < this.lines.length; i++) {
      FontMetrics m = g.getFontMetrics(this.lines[i].font);
      if (m != null) {
        this.lineHeight = Math.max(this.lineHeight, m.getHeight() + 4);
        this.ascent = Math.max(this.ascent, m.getAscent());
        int width = m.stringWidth(this.lines[i].content);
        if (width > this.maxLineWidth) {
          this.maxLineWidth = width;
        }
        this.lines[i].x = width;
      }
      else {
        this.lines[i].x = this.maxLineWidth;
      }
    }
    for (int i = 0; i < this.lines.length; i++) {
      this.lines[i].x = (this.maxLineWidth - this.lines[i].x) / 2;
    }
    this.initialized = true;
    this.scrollOffset = 0;
    g.dispose();
  }


  @Override
  public synchronized void paintComponent(Graphics g) {

    if (!this.initialized) {
      this.calcFontValues();
    }

    int numLines = this.getHeight() / this.lineHeight + 1;

    g.setColor(this.getBackground());
    g.fillRect(0, 0, this.getWidth(), this.getHeight());

    if (!this.timer.isRunning()) {
      return;
    }

    int realOffset = this.scrollOffset - this.getHeight();

    int startIndex = realOffset / this.lineHeight, y =
      (startIndex * this.lineHeight) - realOffset;

    int borderwidth = (this.getWidth() - this.maxLineWidth) / 2;

    Color c = this.getForeground();

    final int top_border = this.ascent, bot_border = 3;

    for (int i = 0; i <= numLines; i++) {
      if ((y >= top_border) && (y <= this.getHeight() - bot_border)
        && ((startIndex + i) >= 0)
                    && ((startIndex + i) < this.lines.length)) {
        g.setFont(this.lines[startIndex + i].font);

        if (y < this.lineHeight + top_border) {
          g.setColor(this.fadeColor(c, y - top_border, this.lineHeight));
        }
        else if (y >= this.getHeight() - this.lineHeight - bot_border) {
          g.setColor(this.fadeColor(c, this.getHeight() - bot_border - y,
            this.lineHeight));
        }
        else {
          g.setColor(c);

          // Pulsieren in der Mitte, aber nur wenn gross genug
          if ((numLines > 4)
            && (this.lines[startIndex + i].font.getStyle() == Font.PLAIN)
                            && (this.flashColor != null)) {
            if ((y <= this.getHeight() / 2)
              && (y >= this.getHeight() / 2 - this.lineHeight / 2)) {
              g.setColor(this.blendColor(this.flashColor, c, this.getHeight()
                / 2 - y,
                              this.lineHeight / 2));
            }
            else if ((y >= this.getHeight() / 2)
              && (y <= this.getHeight() / 2 + this.lineHeight / 2)) {
              g.setColor(this.blendColor(this.flashColor, c, y
                - this.getHeight() / 2,
                              this.lineHeight / 2));
            }
          }
        }

        int x = 0;
        switch (this.align) {
          case LEFT:
            x = VerticalTextScroller.hBorder;
            break;
          case CENTER:
            x = borderwidth + this.lines[startIndex + i].x;
            break;
          case RIGHT:
            x =
              2 * (borderwidth + this.lines[startIndex + i].x)
                - VerticalTextScroller.hBorder;
            break;
          default:
            throw new IllegalStateException("Illegal horizontal alignment: "
              + this.align);
        }

        g.drawString(this.lines[startIndex + i].content, x, y);
      }

      y += this.lineHeight;
    }
  }


  private Color blendColor(Color src, Color dst, int amount, int max) {

    return new Color(src.getRed()
      + (((dst.getRed() - src.getRed()) * amount) / max),
            src.getGreen()
              + (((dst.getGreen() - src.getGreen()) * amount) / max), src
              .getBlue()
                    + (((dst.getBlue() - src.getBlue()) * amount) / max));
  }


  private Color fadeColor(Color c, int amount, int max) {

    return new Color(c.getRed(), c.getGreen(), c.getBlue(), (amount * c
      .getAlpha())
      / max);
  }


  @Override
  public Dimension getMinimumSize() {

    if (!this.initialized) {
      this.calcFontValues();
    }
    Dimension d = super.getMinimumSize();
    d.width =
      Math.max(d.width, this.maxLineWidth + 2 * VerticalTextScroller.hBorder);
    d.height = Math.max(d.height, this.lineHeight * 5);
    return d;
  }


  @Override
  public boolean isOpaque() {

    return true;
  }

  private static class Line {

    public String content;

    public int x;

    public Font font;


    public Line(String content, Font font) {

      this.content = content;
      this.font = font;
      this.x = 0;
    }
  }
}