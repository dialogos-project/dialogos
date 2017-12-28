/*
 * @(#)PatternPanel.java
 * Created on Tue Aug 31 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class PatternPanel
    extends JPanel {

  private Icon pattern;


  public PatternPanel(Icon icon) {

    this.pattern = icon;
  }


  public PatternPanel(Image image) {

    this.pattern = new ImageIcon(image);
  }


  /*
   * public boolean isOpaque() { return true; }
   */

  @Override
  protected void paintComponent(Graphics g) {

    int width = this.getWidth();
    int height = this.getHeight();

    int patternWidth = this.pattern.getIconWidth();
    int patternHeight = this.pattern.getIconHeight();

    for (int y = 0; y < height; y += patternHeight) {
      for (int x = 0; x < width; x += patternWidth) {
        this.pattern.paintIcon(this, g, x, y);
      }
    }
  }


  public Dimension getPatternSize() {

    return new Dimension(this.pattern.getIconWidth(), this.pattern
      .getIconHeight());
  }
}
