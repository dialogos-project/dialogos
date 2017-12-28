/*
 * @(#)BevelPanel.java
 * Created on 12.03.05
 *
 * Copyright (c) 2005 CLT Sprachtechnologie GmbH.
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
import java.awt.Graphics;

import javax.swing.JPanel;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class BevelPanel
    extends JPanel {

  private Color color;


  public BevelPanel(Color color) {

    this.color = color;
  }


  @Override
  protected void paintComponent(Graphics g) {

    int width = this.getWidth() - 1;
    int height = this.getHeight() - 1;
    int center = height / 2;

    g.setColor(this.color);
    g.fillRoundRect(0, 0, width, height, height, height);
    g.setColor(this.color.darker().darker());
    g.drawArc(0, 0, height, height, 90, 90);
    g.drawLine(center, 0, width - center, 0);
    g.drawArc(width - height, 0, height, height, 0, 90);
    g.setColor(this.color.darker());
    g.drawArc(1, 1, height - 2, height - 2, 90, 90);
    g.drawLine(center, 1, width - center, 1);
    g.drawArc(width - height + 1, 1, height - 2, height - 2, 0, 90);

    g.setColor(this.color.brighter().brighter());
    g.drawArc(0, 0, height, height, -180, 90);
    g.drawLine(center, height, width - center, height);
    g.drawArc(width - height, 0, height, height, -90, 90);
    g.setColor(this.color.brighter());
    g.drawArc(1, 1, height - 2, height - 2, -180, 90);
    g.drawLine(center, height - 1, width - center, height - 1);
    g.drawArc(width - height + 1, 1, height - 2, height - 2, -90, 90);
  }
}
