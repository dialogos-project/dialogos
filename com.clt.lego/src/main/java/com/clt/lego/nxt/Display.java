/*
 * @(#)Display.java
 * Created on 10.07.2007 by dabo
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

package com.clt.lego.nxt;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;

/**
 * @author dabo
 * 
 */
public class Display
    extends Module {

  private static final int NORMAL_DISPLAY_OFFSET = 119;
  private static final int POPUP_DISPLAY_OFFSET = 919;
  private static final int DISPLAY_SIZE = 800;

  private BufferedImage image;
  private Graphics2D g;
  private Rectangle dirtyRegion;
  private boolean autoFlush;


  public Display(Nxt brick)
      throws IOException {

    this(brick, true);
  }


  public Display(Nxt brick, boolean autoFlush)
      throws IOException {

    super(brick, "Display.mod");

    this.autoFlush = autoFlush;
    this.image =
      new BufferedImage(NxtImage.SCREEN_WIDTH, NxtImage.SCREEN_HEIGHT,
            BufferedImage.TYPE_INT_ARGB);
    this.g = this.image.createGraphics();
    this.g.setColor(Color.WHITE);
    this.g.fillRect(0, 0, this.getWidth(), this.getHeight());
    this.g.setColor(Color.BLACK);
    this.g.setFont(new Font("Sans", Font.PLAIN, 11));

    this.dirtyRegion = new Rectangle(0, 0, 0, 0);
  }


  public int getWidth() {

    return this.image.getWidth();
  }


  public int getHeight() {

    return this.image.getHeight();
  }


  /**
   * Return a graphics object that you can use to paint to the screen. You need
   * to call {@link #flush} to make your changes actually appear.
   */
  public Graphics getGraphics() {

    return this.g.create();
  }


  public void clear()
      throws IOException {

    Color oldColor = this.g.getColor();
    this.g.setColor(Color.WHITE);
    this.g.fillRect(0, 0, this.getWidth(), this.getHeight());
    this.g.setColor(oldColor);

    this.autoflush(0, 0, this.getWidth(), this.getHeight());
  }


  public void drawLine(int x1, int y1, int x2, int y2)
      throws IOException {

    this.g.drawLine(x1, y1, x2, y2);

    this.autoflush(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2) + 1,
      Math.abs(y1 - y2) + 1);
  }


  public void drawRect(int x, int y, int width, int height)
      throws IOException {

    this.g.drawRect(x, y, width, height);

    this.autoflush(x, y, width + 1, height + 1);
  }


  public void fillRect(int x, int y, int width, int height)
      throws IOException {

    this.g.fillRect(x, y, width, height);

    this.autoflush(x, y, width, height);
  }


  public void drawString(String str, int x, int y)
      throws IOException {

    this.g.drawString(str, x, y);

    FontMetrics fm = this.g.getFontMetrics();
    this.autoflush(x, y - fm.getMaxAscent(), fm.stringWidth(str), fm
      .getMaxAscent()
                + fm.getMaxDescent());
  }


  public void drawImage(Image image, int x, int y, ImageObserver observer)
      throws IOException {

    this.g.drawImage(image, x, y, null);

    this.autoflush(x, y, image.getWidth(observer), image.getHeight(observer));
  }


  public void setColor(Color color) {

    this.g.setColor(color);
  }


  public void setFont(Font font) {

    this.g.setFont(font);
  }


  private void autoflush(int x, int y, int width, int height)
      throws IOException {

    this.dirtyRegion.add(new Rectangle(x, y, width, height));
    if (this.autoFlush) {
      this.flush();
    }
  }


  public void flush()
      throws IOException {

    if ((this.dirtyRegion.width > 0) || (this.dirtyRegion.height > 0)) {
      NxtImage im = new NxtImage(this.image);

      this.write(Display.NORMAL_DISPLAY_OFFSET + Display.DISPLAY_SIZE
        - (im.getLineCount() * im.getWidth()),
                im.getData());
    }
    this.dirtyRegion = new Rectangle(0, 0, 0, 0);
  }


  public void write(Image image)
      throws IOException {

    NxtImage im = new NxtImage(image);

    this.write(Display.NORMAL_DISPLAY_OFFSET + Display.DISPLAY_SIZE
      - (im.getLineCount() * im.getWidth()),
            im.getData());
  }

}
