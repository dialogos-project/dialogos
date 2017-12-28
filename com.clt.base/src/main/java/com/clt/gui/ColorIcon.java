/*
 * @(#)ColorIcon.java
 * Created on 22.02.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * @author dabo
 */
public class ColorIcon
    implements Icon {

  private Color color;
  private int width;
  private int height;


  public ColorIcon(Color color) {

    this(color, 16, 12);
  }


  public ColorIcon(Color color, int width, int height) {

    this.color = color;
    this.width = width;
    this.height = height;
  }


  public Color getColor() {

    return this.color;
  }


  public void paintIcon(Component comp, Graphics g, int x, int y) {

    g.setColor(this.getColor());
    g.fillRect(x, y, this.getIconWidth() - 1, this.getIconHeight() - 1);
    g.setColor(Color.black);
    g.drawRect(x, y, this.getIconWidth() - 1, this.getIconHeight() - 1);
  }


  public int getIconWidth() {

    return this.width;
  }


  public int getIconHeight() {

    return this.height;
  }
}
