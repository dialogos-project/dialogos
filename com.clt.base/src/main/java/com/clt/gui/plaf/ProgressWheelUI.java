/*
 * @(#)ProgressWheelUI.java
 * Created on 07.11.05
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

package com.clt.gui.plaf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ProgressBarUI;
import javax.swing.plaf.UIResource;

/**
 * This class provides an alternate cross-platform UI for progress bars. The
 * design closely resembles the look of progress wheels in Apple's Mac OS X. The
 * determinate version looks like a circular pie while the indeterminate version
 * features rotating translucent stripes. The ProgressWheelUI is stateless, i.e.
 * all components share the same UI object. This in turn means that there is
 * only one timer which updates all indeterminate progress wheels synchronously.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ProgressWheelUI
    extends ProgressBarUI {

  private final int steps;
  private final Color[] indeterminateColors;
  private final Color determinateColor;
  private final Timer timer;
  private final Map<Component, Object> components;
  private int indeterminatePosition;

  private static ProgressWheelUI ui = null;


  /**
   * Return a ProgressWheelUI. All components share the same UI instance.
   */
  public static ComponentUI createUI(JComponent c) {

    // create the UI lazily
    if (ProgressWheelUI.ui == null) {
      ProgressWheelUI.ui = new ProgressWheelUI();
    }
    return ProgressWheelUI.ui;
  }


  /**
   * Sole constructor. (For invocation by subclass constructors, typically
   * implicit.) Users shouldn't instantiated this class direcly. Call createUI()
   * instead.
   */
  protected ProgressWheelUI() {

    this.steps = 12;
    int min = 32;
    int max = 192;

    this.determinateColor = new ColorUIResource(137, 162, 213);

    this.indeterminateColors = new Color[this.steps];

    for (int i = 0; i < this.steps; i++) {
      // colors[i] = new Color(0, 0, 0, min + (max-min) * (steps-i) /
      // steps);
      this.indeterminateColors[i] =
        new Color(0, 0, 0, max
                  - (int)((max - min) * Math.log(i + 1) / Math.log(this.steps)));
    }

    // Use a WeakHashMap for all components that need updating
    // Components are removed on uninstallUI or when they are garbage
    // collected.
    // The timer runs only when there are any components to notify.
    this.components = new WeakHashMap<Component, Object>();

    this.indeterminatePosition = 0;
    this.timer = new Timer(80, null);
    this.timer.setInitialDelay(200);
    this.timer.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        synchronized (ProgressWheelUI.this.timer) {
          if (ProgressWheelUI.this.components.size() == 0) {
            // System.out.println("Stopping timer");
            ProgressWheelUI.this.timer.stop();
          }
        }

        ProgressWheelUI.this.indeterminatePosition =
          (ProgressWheelUI.this.indeterminatePosition + 1)
            % ProgressWheelUI.this.steps;
        for (Component c : ProgressWheelUI.this.components.keySet()) {
          if (c != null) {
            c.repaint();
          }
        }
      }
    });
  }


  /**
   * Setup default colors and start the indeterminate timer if necessary.
   */
  @Override
  public void installUI(JComponent c) {

    Color fg = c.getForeground();
    if ((fg == null) || (fg instanceof UIResource)) {
      c.setForeground(this.determinateColor);
    }

    this.components.put(c, null);
    synchronized (this.timer) {
      if ((this.components.size() > 0) && !this.timer.isRunning()) {
        this.timer.start();
      }
    }
  }


  /**
   * Unregister the component from the indeterminate timer.
   */
  @Override
  public void uninstallUI(JComponent c) {

    this.components.remove(c);
  }


  /**
   * Paint the progress wheel.
   */
  @Override
  public void paint(Graphics g, JComponent c) {

    JProgressBar pb = (JProgressBar)c;

    double center = (Math.min(pb.getWidth() - 1, pb.getHeight() - 1)) / 2.0;
    double h_offset = pb.getWidth() / 2.0 - center;
    double v_offset = pb.getHeight() / 2.0 - center;

    Graphics2D gfx = (Graphics2D)g;
    Color oldColor = gfx.getColor();
    Stroke oldStroke = gfx.getStroke();
    Object oldAA = gfx.getRenderingHint(RenderingHints.KEY_ANTIALIASING);

    /*
     * if (pb.isOpaque()) { g.setColor(pb.getBackground()); g.fillRect(0, 0,
     * pb.getWidth(), pb.getHeight()); }
     */

    gfx.setStroke(new BasicStroke((float)(center / 6.0), BasicStroke.CAP_ROUND,
                                      BasicStroke.JOIN_ROUND));
    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    if (pb.isIndeterminate() || (pb.getMaximum() <= pb.getMinimum())) {
      double in = center * 15.0 / 32.0;
      double out = center * 29.0 / 32.0;

      for (int i = 0; i < 12; i++) {
        double angle = (2.0 * Math.PI * i) / this.steps;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        gfx.setColor(this.indeterminateColors[(this.indeterminatePosition
                        + this.indeterminateColors.length - i)
                        % this.indeterminateColors.length]);

        Line2D line =
          new Line2D.Double(h_offset + center - sin * in, v_offset + center
                        + cos * in, h_offset + center - sin * out, v_offset
            + center + cos * out);
        gfx.draw(line);
      }

    }
    else {
      double percent = (double)(pb.getValue() - pb.getMinimum())
                    / (double)(pb.getMaximum() - pb.getMinimum());
      // catch illegal values in model
      if (percent < 0.0) {
        percent = 0.0;
      }
      else if (percent > 1.0) {
        percent = 1.0;
      }
      double out = center * 29.0 / 32.0;

      gfx.setColor(pb.getForeground());

      Ellipse2D border =
        new Ellipse2D.Double(h_offset + center - out, v_offset + center
                    - out, 2 * out, 2 * out);
      gfx.draw(border);

      Arc2D pie =
        new Arc2D.Double(border.getX(), border.getY(), border.getWidth(),
          border
                    .getHeight(), 90, -percent * 360, Arc2D.PIE);
      gfx.fill(pie);
    }

    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAA);
    gfx.setStroke(oldStroke);
    gfx.setColor(oldColor);
  }


  /**
   * Return the preferred size (16x16).
   */
  @Override
  public Dimension getPreferredSize(JComponent c) {

    return new Dimension(16, 16);
  }
}
