/*
 * @(#)ImageBorder.java
 * Created on 06.04.05
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

package com.clt.gui.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

/**
 * Draw a border by filling the component's background with an image. It the
 * component is larger than the image, the image will be split into four
 * quarters that paint the corners of the component. The middle space will be
 * painted by copying the center pixels of the image.
 * <p>
 * Example: If you pass an image consisting of a circle, and the component is
 * larger than your image, the image will be extracted such that it resembles a
 * rectangle with round corners.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ImageBorder
    implements Border {

  private SplitImage image, imageWhenDisabled, imageWhenFocused;

  private Insets insets = null;

  private boolean scale = true;


  /** Create an ImageBorder using the given image. */
  public ImageBorder(Image image) {

    this(image, null, null);
  }


  /**
   * Create an ImageBorder using separate images for the normal, disabled and
   * focused state of the component. The disabled and the focused image may be
   * null.
   */
  public ImageBorder(Image image, Image imageWhenDisabled,
      Image imageWhenFocused) {

    this(image, imageWhenDisabled, imageWhenFocused, null);
  }


  /**
   * Create an ImageBorder using separate images for the normal, disabled and
   * focused state of the component. The disabled and the focused image may be
   * null. Per default, the border does not have any insets but instead is
   * painted as the background of the component. By passing non-null insets, you
   * can make sure that the border's size extends the component's size.
   */
  public ImageBorder(Image image, Image imageWhenDisabled,
      Image imageWhenFocused, Insets insets) {

    if (image == null) {
      throw new IllegalArgumentException("Image may not be null");
    }
    this.image = new SplitImage(image);
    if (imageWhenDisabled != null) {
      this.imageWhenDisabled = new SplitImage(imageWhenDisabled);
    }
    if (imageWhenFocused != null) {
      this.imageWhenFocused = new SplitImage(imageWhenFocused);
    }
    this.setInsets(insets);
  }


  /** Constructor using icons instead of images. */
  public ImageBorder(Icon image) {

    this(image, null, null);
  }


  /** Constructor using icons instead of images. */
  public ImageBorder(Icon image, Icon imageWhenDisabled, Icon imageWhenFocused) {

    this(image, imageWhenDisabled, imageWhenFocused, null);
  }


  /** Constructor using icons instead of images. */
  public ImageBorder(Icon image, Icon imageWhenDisabled, Icon imageWhenFocused,
      Insets insets) {

    if (image == null) {
      throw new IllegalArgumentException("Image may not be null");
    }
    this.image = new SplitImage(image);
    if (imageWhenDisabled != null) {
      this.imageWhenDisabled = new SplitImage(imageWhenDisabled);
    }
    if (imageWhenFocused != null) {
      this.imageWhenFocused = new SplitImage(imageWhenFocused);
    }
    this.setInsets(insets);
  }


  public Dimension getPreferredSize() {

    return this.image.getPreferredSize();
  }


  public void setInsets(Insets insets) {

    this.insets = insets;
  }


  /**
   * Select, whether corners are scaled or clipped. Per default, the image is
   * scaled down if the component is smaller than the image. You may call
   * <code>setScaleCorners(false)</code> if you instead want to clip the image
   * corners in this case. This does not have any effect if the component is
   * larger than the image.
   */
  public void setScaleCorners(boolean scale) {

    this.scale = scale;
  }


  public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height) {

    SplitImage im = this.image;
    if (!c.isEnabled() && (this.imageWhenDisabled != null)) {
      im = this.imageWhenDisabled;
    }
    else if (c.hasFocus() && (this.imageWhenFocused != null)) {
      im = this.imageWhenFocused;
    }
    im.paint(c, g, x, y, width, height);
  }


  public Insets getBorderInsets(Component c) {

    Insets i =
      this.insets == null ? new Insets(0, 0, 0, 0) : (Insets)this.insets
        .clone();
    if (c instanceof JTextComponent) {
      Insets margin = ((JTextComponent)c).getMargin();
      if (margin != null) {
        i.left += margin.left;
        i.right += margin.right;
        i.top += margin.top;
        i.bottom += margin.bottom;
      }
    }
    return i;
  }


  public boolean isBorderOpaque() {

    // image might be transparent
    return false;
  }

  private class SplitImage {

    private static final int left_top = 0;
    private static final int left_bottom = 1;
    private static final int right_top = 2;
    private static final int right_bottom = 3;

    private Image top, left, bottom, right;

    private Image[] corner = new Image[4];

    private Color color;

    int cornerwidth, cornerheight;

    int preferredWidth;

    int preferredHeight;


    public SplitImage(Image image) {

      this.init(image);
    }


    public SplitImage(Icon icon) {

      if (icon instanceof ImageIcon) {
        this.init(((ImageIcon)icon).getImage());
      }
      else {
        BufferedImage im =
          new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                    BufferedImage.TYPE_INT_ARGB);
        Graphics g = im.getGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        this.init(im);
      }
    }


    private void init(Image im) {

      BufferedImage image;
      if (im instanceof BufferedImage) {
        image = (BufferedImage)im;
      }
      else {
        image = new BufferedImage(im.getWidth(null), im.getHeight(null),
                    BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.drawImage(im, 0, 0, null);
        g.dispose();
      }

      int width = image.getWidth(null);
      int height = image.getHeight(null);

      this.corner[SplitImage.left_top] =
        image.getSubimage(0, 0, width / 2, height / 2);
      this.corner[SplitImage.left_bottom] =
        image.getSubimage(0, (height + 1) / 2, width / 2, height / 2);
      this.corner[SplitImage.right_top] =
        image.getSubimage((width + 1) / 2, 0, width / 2, height / 2);
      this.corner[SplitImage.right_bottom] =
        image.getSubimage((width + 1) / 2, (height + 1) / 2, width / 2,
                height / 2);

      if (width % 2 == 0) {
        this.top = image.getSubimage(width / 2 - 1, 0, 2, height / 2);
        this.bottom =
          image.getSubimage(width / 2 - 1, (height + 1) / 2, 2, height / 2);
      }
      else {
        this.top = image.getSubimage(width / 2, 0, 1, height / 2);
        this.bottom =
          image.getSubimage(width / 2, (height + 1) / 2, 1, height / 2);
      }
      if (height % 2 == 0) {
        this.left = image.getSubimage(0, height / 2 - 1, width / 2, 2);
        this.right =
          image.getSubimage((width + 1) / 2, height / 2 - 1, width / 2, 2);
      }
      else {
        this.left = image.getSubimage(0, height / 2, width / 2, 1);
        this.right =
          image.getSubimage((width + 1) / 2, height / 2, width / 2, 1);
      }

      BufferedImage center = image.getSubimage(width / 2, height / 2, 1, 1);
      this.color = new Color(center.getRGB(0, 0), true);

      this.cornerwidth = this.corner[SplitImage.left_top].getWidth(null);
      this.cornerheight = this.corner[SplitImage.left_top].getHeight(null);

      this.preferredWidth = width;
      this.preferredHeight = height;
    }


    public Dimension getPreferredSize() {

      return new Dimension(this.preferredWidth, this.preferredHeight);
    }


    public void paint(Component c, Graphics g, int x, int y, int width,
        int height) {

      int cornerwidth = Math.min(this.cornerwidth, width / 2);
      int cornerheight = Math.min(this.cornerheight, height / 2);
      if (ImageBorder.this.scale) {
        this.paintCornerScaled(c, g, SplitImage.left_top, x, y, cornerwidth,
          cornerheight);
        this.paintCornerScaled(c, g, SplitImage.left_bottom, x, y + height
          - cornerheight, cornerwidth,
                    cornerheight);
        this.paintCornerScaled(c, g, SplitImage.right_top, x + width
          - cornerwidth, y, cornerwidth,
                    cornerheight);
        this.paintCornerScaled(c, g, SplitImage.right_bottom, x + width
          - cornerwidth, y + height
                        - cornerheight, cornerwidth, cornerheight);

        if (width > 2 * cornerwidth) {
          g.drawImage(this.top, x + cornerwidth, y, width - 2 * cornerwidth,
            cornerheight, c);
          g.drawImage(this.bottom, x + cornerwidth, y + height - cornerheight,
            width - 2
                            * cornerwidth, cornerheight, c);
        }

        if (height > 2 * cornerheight) {
          g.drawImage(this.left, x, y + cornerheight, cornerwidth, height - 2
            * cornerheight,
                        c);
          g.drawImage(this.right, x + width - cornerwidth, y + cornerheight,
            cornerwidth,
                        height - 2 * cornerheight, c);
        }
      }
      else {
        this.paintCornerClipped(c, g, SplitImage.left_top, x, y, x, y,
          cornerwidth, cornerheight);
        this.paintCornerClipped(c, g, SplitImage.left_bottom, x, y + height
          - this.cornerheight, x, y
                        + height - cornerheight, cornerwidth, cornerheight);
        this.paintCornerClipped(c, g, SplitImage.right_top, x + width
          - this.cornerwidth, y, x + width
                        - cornerwidth, y, cornerwidth, cornerheight);
        this.paintCornerClipped(c, g, SplitImage.right_bottom, x + width
          - this.cornerwidth, y + height
                        - this.cornerheight, x + width - cornerwidth, y
          + height - cornerheight,
                    cornerwidth, cornerheight);
        Shape clip = g.getClip();
        if (width > 2 * cornerwidth) {
          g.clipRect(x + cornerwidth, y, width - 2 * cornerwidth, cornerheight);
          g.drawImage(this.top, x + cornerwidth, y, width - 2 * cornerwidth,
                        this.cornerheight, c);
          g.setClip(clip);
          g.clipRect(x + cornerwidth, y + height - cornerheight, width - 2
            * cornerwidth,
                        cornerheight);
          g.drawImage(this.bottom, x + cornerwidth, y + height
            - this.cornerheight, width - 2
                            * cornerwidth, this.cornerheight, c);
          g.setClip(clip);
        }

        if (height > 2 * cornerheight) {
          g.clipRect(x, y + cornerheight, cornerwidth, height - 2
            * cornerheight);
          g.drawImage(this.left, x, y + cornerheight, this.cornerwidth, height
            - 2
                            * cornerheight, c);
          g.setClip(clip);
          g.clipRect(x + width - cornerwidth, y + cornerheight, cornerwidth,
            height - 2
                            * cornerheight);
          g.drawImage(this.right, x + width - this.cornerwidth, y
            + cornerheight,
                        this.cornerwidth, height - 2 * cornerheight, c);
          g.setClip(clip);
        }
      }

      if ((width > 2 * cornerwidth) && (height > 2 * cornerheight)) {
        g.setColor(this.color);
        g.fillRect(x + cornerwidth, y + cornerheight, width - 2 * cornerwidth,
          height - 2
                        * cornerheight);
      }
    }


    private void paintCornerScaled(Component c, Graphics g, int corner, int x,
        int y,
                int width, int height) {

      Image image = this.corner[corner];
      g.drawImage(image, x, y, width, height, c);
    }


    private void paintCornerClipped(Component c, Graphics g, int corner, int x,
        int y,
                int clipx, int clipy, int clipwidth, int clipheight) {

      Image image = this.corner[corner];
      Shape clip = g.getClip();
      g.clipRect(clipx, clipy, clipwidth, clipheight);
      g.drawImage(image, x, y, c);
      g.setClip(clip);
    }

  }
}
