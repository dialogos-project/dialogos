/*
 * @(#)ScrollLabel.java
 * Created on 26.04.05
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * This class presents an iTunes-like single line of text, whose content is a
 * number of text lines that keep scrolling through.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ScrollLabel
    extends JComponent {

  // Ausrichtung der Labels
  public static final int LEFT = SwingConstants.LEFT,
      CENTER = SwingConstants.CENTER,
            RIGHT = SwingConstants.RIGHT;

  private String[] text;

  private int alignment = ScrollLabel.LEFT;

  private int currentLine = 0;

  private int currentOffset = 0;

  private int scrollPause = 5000;

  private int scrollDuration = 1000;

  javax.swing.Timer timer;


  private ScrollLabel() {

    JLabel l = new JLabel();
    this.setForeground(l.getForeground());
    this.setBackground(l.getBackground());
    this.setFont(l.getFont());

    this.timer = new javax.swing.Timer(1000, new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        int height = ScrollLabel.this.getHeight();
        if (height > 0) {
          ScrollLabel.this.currentOffset++;
          if (ScrollLabel.this.currentOffset >= height) {
            ScrollLabel.this.currentOffset = 0;
            ScrollLabel.this.currentLine =
              (ScrollLabel.this.currentLine + 1) % ScrollLabel.this.text.length;
            ScrollLabel.this.timer.setDelay(ScrollLabel.this.scrollPause);
          }
          else {
            ScrollLabel.this.timer.setDelay(ScrollLabel.this.scrollDuration
              / height);
          }
          ScrollLabel.this.repaint();
        }
      }
    });
  }


  public ScrollLabel(String label) {

    this(label, ScrollLabel.LEFT);
  }


  public ScrollLabel(String label, int alignment) {

    this();
    this.setText(label);
    this.setAlignment(alignment);
  }


  public ScrollLabel(String[] labels) {

    this(labels, ScrollLabel.LEFT);
  }


  public ScrollLabel(String[] labels, int alignment) {

    this();
    this.setText(labels);
    this.setAlignment(alignment);
  }


  public void setDelay(int scrollDuration, int scrollPause) {

    this.scrollDuration = scrollDuration;
    this.scrollPause = scrollPause;
  }


  @Override
  public void addNotify() {

    super.addNotify();
    this.timer.start();
  }


  @Override
  public void removeNotify() {

    this.timer.stop();
    super.removeNotify();
  }


  public int getAlignment() {

    return this.alignment;
  }


  public void setAlignment(int alignment) {

    switch (alignment) {
      case LEFT:
      case RIGHT:
      case CENTER:
        this.alignment = alignment;
        break;
      default:
        throw new IllegalArgumentException("improper alignment: " + alignment);
    }
  }


  public void setText(String label) {

    if (label == null) {
      label = "";
    }

    Vector<String> lines = new Vector<String>();
    int start, end;

    start = 0;
    end = 0;

    while (end < label.length()) {
      end = label.indexOf('\n', start);
      if (end == -1) {
        end = label.length();
      }
      lines.add(label.substring(start, end));
      start = end + 1;
    }

    this.text = new String[lines.size()];
    lines.copyInto(this.text);
  }


  public String getText() {

    StringBuilder s = new StringBuilder(this.text[0]);

    for (int i = 1; i < this.text.length; i++) {
      s.append("\n" + this.text[i]);
    }

    return s.toString();
  }


  public void setText(String[] labels) {

    if (labels == null) {
      throw new IllegalArgumentException("Null array passed to setText");
    }
    if (labels.length == 0) {
      throw new IllegalArgumentException("Empty array passed to setText");
    }

    this.text = new String[labels.length];
    System.arraycopy(labels, 0, this.text, 0, labels.length);
  }


  public String[] getTextLines() {

    return this.text;
  }


  @Override
  public void paintComponent(Graphics g) {

    FontMetrics m = g.getFontMetrics();
    int height = m.getHeight(), ascent = m.getMaxAscent();

    Color saveColor = g.getColor();
    g.setColor(this.getForeground());

    String line = this.text[this.currentLine];
    String next = this.text[(this.currentLine + 1) % this.text.length];

    int vpos = ascent - this.currentOffset, hpos;
    switch (this.alignment) {
      case LEFT:
        hpos = 0;
        break;
      case RIGHT:
        hpos = this.getSize().width - m.stringWidth(line);
        break;
      case CENTER:
        hpos = (this.getSize().width - m.stringWidth(line)) / 2;
        break;
      default:
        throw new IllegalArgumentException("improper alignment: "
          + this.alignment);
    }

    g.drawString(line, hpos, vpos);

    switch (this.alignment) {
      case LEFT:
        hpos = 0;
        break;
      case RIGHT:
        hpos = this.getSize().width - m.stringWidth(next);
        break;
      case CENTER:
        hpos = (this.getSize().width - m.stringWidth(next)) / 2;
        break;
      default:
        throw new IllegalArgumentException("improper alignment: "
          + this.alignment);
    }
    g.drawString(next, hpos, vpos + height);

    g.setColor(saveColor);
  }


  @Override
  public Dimension getMinimumSize() {

    Graphics g = this.getGraphics();
    if (g == null) {
      return new Dimension(0, 0);
    }
    FontMetrics m = g.getFontMetrics();
    int height = m.getHeight();
    g.dispose();

    int width = 0;
    for (int i = 0; i < this.text.length; i++) {
      width = Math.max(width, m.stringWidth(this.text[i]));
    }

    return new Dimension(width, height);
  }


  @Override
  public Dimension getPreferredSize() {

    return this.getMinimumSize();
  }
}