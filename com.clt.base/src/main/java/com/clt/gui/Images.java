/*
 * @(#)Images.java
 * Created on 27.09.2006 by dabo
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

package com.clt.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.MissingResourceException;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.clt.io.FileFormatException;
import com.clt.resources.ResourceFormatException;

/**
 * @author dabo
 * 
 */
public class Images {

  public static BufferedImage createReflectedPicture(BufferedImage image) {

    int imageWidth = image.getWidth();
    int imageHeight = image.getHeight();

    BufferedImage gradient = Images.createGradientMask(imageWidth, imageHeight);
    BufferedImage buffer =
      Images.createReflection(image, imageWidth, imageHeight);

    Images.applyAlphaMask(gradient, buffer, imageWidth, imageHeight);

    return buffer;
  }


  private static BufferedImage createGradientMask(int width, int height) {

    BufferedImage gradient =
      new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = gradient.createGraphics();
    GradientPaint painter =
      new GradientPaint(0.0f, 0.0f, new Color(1.0f, 1.0f, 1.0f, 0.5f),
            0.0f, height / 2.0f, new Color(1.0f, 1.0f, 1.0f, 0.0f));
    g.setPaint(painter);
    g.fill(new Rectangle2D.Double(0, 0, width, height));

    g.dispose();
    gradient.flush();

    return gradient;
  }


  private static BufferedImage createReflection(BufferedImage image, int width,
      int height) {

    BufferedImage buffer =
      new BufferedImage(width, height << 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = buffer.createGraphics();

    g.drawImage(image, null, null);
    g.translate(0, height << 1);

    AffineTransform reflectTransform =
      AffineTransform.getScaleInstance(1.0, -1.0);
    g.drawImage(image, reflectTransform, null);
    g.translate(0, -(height << 1));

    g.dispose();

    return buffer;
  }


  private static void applyAlphaMask(BufferedImage gradient,
      BufferedImage buffer, int width,
            int height) {

    Graphics2D g2 = buffer.createGraphics();
    g2.setComposite(AlphaComposite.DstOut);
    g2.drawImage(gradient, null, 0, height);
    g2.dispose();
  }


  public static Image rotate(Image image, boolean left) {

    BufferedImage src;
    if (image instanceof BufferedImage) {
      src = (BufferedImage)image;
    }
    else {
      src = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = src.createGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
    }

    int width = src.getWidth(null);
    int height = src.getHeight(null);
    BufferedImage dst =
      new BufferedImage(height, width, BufferedImage.TYPE_INT_ARGB);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        if (left) {
          dst.setRGB(y, width - 1 - x, src.getRGB(x, y));
        }
        else {
          dst.setRGB(height - 1 - y, x, src.getRGB(x, y));
        }
      }
    }

    return dst;
  }


  public static Icon rotate(Icon icon, boolean left) {

    return new ImageIcon(Images.rotate(Images.getImage(icon), left));
  }


  public static Image adjustBrightness(Image image, final float fraction) {

    RGBImageFilter filter = new RGBImageFilter() {

      {
        this.canFilterIndexColorModel = true;
      }


      @Override
      public int filterRGB(int x, int y, int rgb) {

        // keep alpha, adjust rgb
        return ((rgb & 0xff000000) | this.filter(rgb, 16, fraction)
          | this.filter(rgb, 8, fraction) | this.filter(
                    rgb, 0, fraction));
      }


      private int filter(int rgb, int bits, float fraction) {

        int component = (rgb >> bits) & 0x000000ff;
        component = Math.round(component * fraction);
        component = Math.max(0, Math.min(255, component));
        return component << bits;
      }
    };
    ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
    Image result = Toolkit.getDefaultToolkit().createImage(prod);
    return result;
  }


  public static Icon adjustBrightness(Icon icon, float fraction) {

    if (icon instanceof ImageIcon) {
      return new ImageIcon(Images.adjustBrightness(
        ((ImageIcon)icon).getImage(), fraction));
    }
    else {
      BufferedImage im =
        new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = im.createGraphics();
      icon.paintIcon(null, g, 0, 0);
      g.dispose();

      return new ImageIcon(Images.adjustBrightness(im, fraction));
    }
  }


  public static Image adjustOpacity(Image image, final float fraction) {

    RGBImageFilter filter = new RGBImageFilter() {

      {
        this.canFilterIndexColorModel = true;
      }


      @Override
      public int filterRGB(int x, int y, int rgb) {

        // adjust alpha, keep rgb
        return (rgb & 0x00ffff) | this.filter(rgb, 24, fraction);
      }


      private int filter(int rgb, int bits, float fraction) {

        int component = (rgb >> bits) & 0x000000ff;
        component = Math.round(component * fraction);
        component = Math.max(0, Math.min(255, component));
        return component << bits;
      }
    };
    ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
    Image result = Toolkit.getDefaultToolkit().createImage(prod);
    return result;
  }


  public static Icon adjustOpacity(Icon icon, float fraction) {

    if (icon instanceof ImageIcon) {
      return new ImageIcon(Images.adjustOpacity(((ImageIcon)icon).getImage(),
        fraction));
    }
    else {
      BufferedImage im =
        new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = im.createGraphics();
      icon.paintIcon(null, g, 0, 0);
      g.dispose();

      return new ImageIcon(Images.adjustOpacity(im, fraction));
    }
  }


  public static Image blend(Image image, final Color color, final float opacity) {

    final int argb = color.getRGB();
    RGBImageFilter filter = new RGBImageFilter() {

      {
        this.canFilterIndexColorModel = true;
      }


      @Override
      public int filterRGB(int x, int y, int rgb) {

        // keep alpha, adjust rgb
        return (rgb & 0xff000000) | this.filter(rgb, argb, 16, opacity)
                        | this.filter(rgb, argb, 8, opacity)
          | this.filter(rgb, argb, 0, opacity);
      }


      private int filter(int rgb, int argb, int bits, float opacity) {

        int alpha = (rgb >> 24) & 0x000000ff;
        int component = (rgb >> bits) & 0x000000ff;
        int overlay = (argb >> bits) & 0x000000ff;

        component =
          Math.round(component * (1.0f - opacity) + (overlay * opacity * alpha)
                        / 255.0f);
        component = Math.max(0, Math.min(255, component));
        return component << bits;
      }
    };
    ImageProducer prod = new FilteredImageSource(image.getSource(), filter);
    Image result = Toolkit.getDefaultToolkit().createImage(prod);
    return result;
  }


  public static Icon blend(Icon icon, Color color, float opacity) {

    if (icon instanceof ImageIcon) {
      return new ImageIcon(Images.blend(((ImageIcon)icon).getImage(), color,
        opacity));
    }
    else {
      BufferedImage im =
        new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = im.createGraphics();
      icon.paintIcon(null, g, 0, 0);
      g.dispose();

      return new ImageIcon(Images.blend(im, color, opacity));
    }
  }


  public static ImageIcon loadBuiltin(String name) {

    // URL path = ClassLoader.getSystemResource("com/clt/resources/" + name);
    URL path = com.clt.resources.DynamicResourceBundle.class.getResource(name);
    if (path == null) {
      throw new MissingResourceException("Could not find image", Images.class
        .getName(), name);
    }
    return new ImageIcon(path);
  }


  public static ImageIcon load(Object owner, String name) {

    // zuerst versuchen wir, aus dem jar-file zu laden
    URL path = owner.getClass().getClassLoader().getResource("images/" + name);
    if (path != null) {
      ImageIcon icon = new ImageIcon(path);
      if (icon.getImage() == null) {
        throw new ResourceFormatException("Unknown image format");
      }
      else {
        return icon;
      }
    }
    else {
      try {
        return Images.load(new File("images", name));
      } catch (IOException exn) {
        throw new MissingResourceException("Could not load image",
                    owner.getClass().getName(), name);
      }
    }
  }


  public static ImageIcon load(String name) {

    // zuerst versuchen wir, aus dem jar-file zu laden
    URL path = ClassLoader.getSystemResource("images/" + name);
    if (path != null) {
      return new ImageIcon(path);
    }
    else {
      path = GUI.class.getClassLoader().getResource("images/" + name);
      if (path != null) {
        ImageIcon icon = new ImageIcon(path);
        if (icon.getImage() == null) {
          throw new ResourceFormatException("Unknown image format");
        }
        else {
          return icon;
        }
      }
      else {
        try {
          return Images.load(new File("images", name));
        } catch (IOException exn) {
          throw new MissingResourceException("Could not load image",
            "com.clt.gui.GUI",
                        name);
        }
      }
    }
  }


  public static ImageIcon load(File f)
      throws IOException {

    if (f.exists()) {
      ImageIcon icon = new ImageIcon(f.getAbsolutePath());
      if (icon.getImage() == null) {
        throw new FileFormatException("Unknown image format");
      }
      else {
        return icon;
      }
    }
    else {
      throw new FileNotFoundException(f.toString());
    }
  }


  public static Image getImage(Icon icon) {

    if (icon == null) {
      return null;
    }
    else if (icon instanceof ImageIcon) {
      return ((ImageIcon)icon).getImage();
    }
    else {
      BufferedImage im =
        new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = im.getGraphics();
      icon.paintIcon(null, g, 0, 0);
      g.dispose();
      return im;
    }
  }


  public static BufferedImage getBufferedImage(Image image) {

    if (image == null) {
      return null;
    }
    else if (image instanceof BufferedImage) {
      return (BufferedImage)image;
    }
    else {
      BufferedImage im =
        new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
      Graphics g = im.getGraphics();
      g.drawImage(image, 0, 0, null);
      g.dispose();
      return im;
    }
  }


  public static Image[] split(Image im, int numTiles, boolean separateMask) {

    return Images.split(im, numTiles, 1, separateMask);
  }


  public static Image[] split(Image im, int columns, int rows,
      boolean separateMask) {

    BufferedImage image =
      new BufferedImage(im.getWidth(null), im.getHeight(null),
            BufferedImage.TYPE_INT_ARGB);
    Graphics g = image.getGraphics();
    g.drawImage(im, 0, 0, null);
    g.dispose();

    BufferedImage[] tiles = new BufferedImage[columns * rows];
    int width = image.getWidth(null) / columns;
    int height = image.getHeight(null) / rows;
    if (separateMask) {
      height = height / 2;
    }
    for (int i = 0; i < columns * rows; i++) {
      tiles[i] =
        image.getSubimage((i % columns) * width, (i / columns) * height, width,
                height);
      if (separateMask) {
        BufferedImage mask =
          image.getSubimage((i % columns) * width, (rows + i / columns)
                        * height, width, height);
        int[] rgb = tiles[i].getRGB(0, 0, width, height, null, 0, width);
        int[] maskRgb = mask.getRGB(0, 0, width, height, null, 0, width);
        for (int n = 0; n < rgb.length; n++) {
          // Use NTSC conversion formula.
          int gray =
            (int)((0.30 * ((maskRgb[n] >> 16) & 0xff) + 0.59
                            * ((maskRgb[n] >> 8) & 0xff) + 0.11 * ((maskRgb[n]) & 0xff)));
          // int gray = (maskRgb[n] >> 8) & 0xff;

          rgb[n] = (rgb[n] & 0x00FFFFFF) | ((0xff - gray) << 24);
        }
        tiles[i].setRGB(0, 0, width, height, rgb, 0, width);
      }
    }

    return tiles;
  }

}
