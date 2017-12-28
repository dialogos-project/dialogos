/*
 * @(#)ImageChooser.java
 * Created on 11.04.2006 by dabo
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * @author dabo
 */
public class ImageChooser
    extends JComponent {

  private Image selection;
  private ArrayList<Image> images;
  private int padx = 4;
  private int pady = 4;

  private transient int maxIconWidth = 0;
  private transient int maxIconHeight = 0;
  private transient Map<Image, Rectangle> layout =
    new HashMap<Image, Rectangle>();


  public ImageChooser() {

    this(new Image[0]);
  }


  public ImageChooser(Image[] images) {

    this(Arrays.asList(images));
  }


  public ImageChooser(Collection<? extends Image> images) {

    this.images = new ArrayList<Image>();
    if (images != null) {
      this.addAll(images);
    }

    this.setBackground(Color.WHITE);
    this.setForeground(new Color(192, 212, 255));

    this.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent evt) {

        synchronized (ImageChooser.this.layout) {
          Point p = evt.getPoint();
          for (Image im : ImageChooser.this.layout.keySet()) {
            if (ImageChooser.this.layout.get(im).contains(p)) {
              ImageChooser.this.selection = im;
              ImageChooser.this.repaint();
              break;
            }
          }
        }
      }
    });

    this.setOpaque(true);

    if (this.images.size() > 0) {
      this.selection = this.images.iterator().next();
    }
    else {
      this.selection = null;
    }
  }


  public void add(Image image) {

    this.images.add(image);
    this.selection = image;

    this.repaint();
  }


  public void addAll(Collection<? extends Image> images) {

    this.images.addAll(images);

    this.repaint();
  }


  private Map<Image, Rectangle> calculateLayout(Map<Image, Rectangle> layout,
      int totalWidth) {

    layout.clear();
    int x = 0;
    int y = 0;

    for (Image im : this.images) {
      this.maxIconWidth =
        Math.max(this.maxIconWidth, im.getWidth(this) + 2 * this.padx);
      this.maxIconHeight =
        Math.max(this.maxIconHeight, im.getHeight(this) + 2 * this.pady);
    }

    for (Image im : this.images) {
      if ((x > 0) && (x + this.maxIconWidth > totalWidth)) {
        x = 0;
        y += this.maxIconHeight;
      }

      layout
        .put(im, new Rectangle(x, y, this.maxIconWidth, this.maxIconHeight));
      x += this.maxIconWidth;
    }

    return layout;
  }


  @Override
  protected void paintComponent(Graphics g) {

    synchronized (this.layout) {
      this.layout = this.calculateLayout(this.layout, this.getWidth());

      if (super.isOpaque()) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
      }

      for (Image im : this.layout.keySet()) {
        Rectangle r = this.layout.get(im);
        if (im == this.selection) {
          g.setColor(this.getForeground());
          g.fillRect(r.x, r.y, r.width, r.height);
        }
        else {
          g.setColor(Color.BLACK);
        }

        g.drawImage(im, r.x + (r.width - im.getWidth(this)) / 2, r.y
                        + (r.height - im.getHeight(this)) / 2, this);
      }
    }
  }


  @Override
  public Dimension getPreferredSize() {

    return this.getMinimumSize();
  }


  @Override
  public Dimension getMinimumSize() {

    Map<Image, Rectangle> layout =
      this.calculateLayout(new HashMap<Image, Rectangle>(), 200);
    int width = 100;
    int height = 100;
    for (Rectangle r : layout.values()) {
      width = Math.max(width, r.x + r.width);
      height = Math.max(height, r.y + r.height);
    }
    return new Dimension(width, height);
  }


  @Override
  public boolean isOpaque() {

    return super.isOpaque() && (this.getBackground().getAlpha() == 0xff);
  }


  public Image getSelectedImage() {

    return this.selection;
  }


  public Collection<Image> getImages() {

    return Collections.unmodifiableCollection(this.images);
  }


  public static Image selectImage(Component parent,
      Collection<? extends Image> images) {

    return ImageChooser.selectImage(parent, images, null);
  }


  public static Image selectImage(Component parent,
      Collection<? extends Image> images,
            final Dimension maxImageSize) {

    final ImageChooser chooser = new ImageChooser(images);
    final JScrollPane jsp =
      GUI.createScrollPane(chooser,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JButton add = new CmdButton(GUI.getString("Add") + "...", new Runnable() {

      public void run() {

        File f = new FileChooser().standardGetFile(chooser);
        if (f != null) {
          try {
            Image image = chooser.getToolkit().createImage(f.getAbsolutePath());
            if (image != null) {
              // make sure image is fully loaded
              image = new ImageIcon(image).getImage();

              // scale if necessary
              if ((maxImageSize != null) && (maxImageSize.width > 0)
                                    && (maxImageSize.height > 0))
                            {
                              double hRatio = (double)maxImageSize.width
                                        / (double)image.getWidth(null);
                              double vRatio = (double)maxImageSize.height
                                        / (double)image.getHeight(null);

                              if ((hRatio < 1.0) || (vRatio < 1.0)) {
                                double scale = Math.min(hRatio, vRatio);
                                image =
                                  image.getScaledInstance(
                                        (int)(image.getWidth(null) * scale),
                                        (int)(image.getHeight(null) * scale),
                                    Image.SCALE_SMOOTH);
                                image = new ImageIcon(image).getImage();
                              }
                            }

                            chooser.add(image);
                            chooser.revalidate();
                            jsp.revalidate();
                          }
                        }
                    catch (Exception exn) {
                      OptionPane.error(chooser, exn);
                    }
                  }
                }
    });
    if (OptionPane.confirm(parent, new JComponent[] {
      new JLabel("Please choose an image"),
                jsp, add }, "Choose Image", OptionPane.OK_CANCEL_OPTION) == OptionPane.OK) {
      return chooser.getSelectedImage();
    }
    else {
      return null;
    }
  }
}
