/*
 * @(#)Buttons.java
 * Created on 07.04.05
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

import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.UIManager;

import com.clt.util.Platform;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Buttons {

  public static final int NORMAL = 1;
  public static final int DISABLED = 2;
  public static final int PRESSED = 3;
  public static final int SELECTED = 4;
  public static final int ROLLOVER = 5;
  public static final int ROLLOVER_SELECTED = 6;
  public static final int DISABLED_SELECTED = 7;


  private static JButton createButton() {

    JButton button = new JButton() {

      @Override
      public boolean isFocusTraversable() {

        return false;
      }


      @Override
      public boolean isFocusable() {

        return false;
      }
    };
    button.setFocusPainted(false);
    return button;
  }


  public static void showOnlyIcon(AbstractButton button) {

    button.setMargin(new Insets(0, 0, 0, 0));
    button.setBorder(null);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
  }


  private static Icon loadImage(String name) {

    try {
      return Images.load(name);
    } catch (Exception exn) {
      return null;
    }
  }


  private static Icon loadImage(String name, String suffix) {

    Icon im = null;

    if (suffix != null) {
      Buttons.loadImage(name + suffix);
    }
    else {
      if (im == null) {
        im = Buttons.loadImage(name + ".png");
      }

      if (im == null) {
        im = Buttons.loadImage(name + ".gif");
      }

      if (im == null) {
        im = Buttons.loadImage(name + ".jpg");
      }
    }

    return im;
  }


  public static JButton loadImageButton(String prefix, String suffix) {

    JButton b = Buttons.createButton();
    b.setIcon(Images.load(prefix + suffix));

    Icon icon = Buttons.loadImage(prefix + "Disabled", suffix);
    if (icon != null) {
      b.setDisabledIcon(icon);
    }

    icon = Buttons.loadImage(prefix + "Pressed", suffix);
    if (icon != null) {
      b.setPressedIcon(icon);
    }

    icon = Buttons.loadImage(prefix + "Selected", suffix);
    if (icon != null) {
      b.setSelectedIcon(icon);
    }

    icon = Buttons.loadImage(prefix + "DisabledSelected", suffix);
    if (icon != null) {
      b.setDisabledSelectedIcon(icon);
    }

    icon = Buttons.loadImage(prefix + "Rollover", suffix);
    if (icon != null) {
      b.setRolloverIcon(icon);
    }

    icon = Buttons.loadImage(prefix + "RolloverSelected", suffix);
    if (icon != null) {
      b.setRolloverSelectedIcon(icon);
    }

    Buttons.showOnlyIcon(b);

    return b;
  }


  public static JButton createImageButton(Image image) {

    return Buttons.createImageButton(new ImageIcon(image));
  }


  public static JButton createImageButton(Icon icon) {

    JButton button = Buttons.createButton();
    button.setIcon(icon);
    button.setPressedIcon(Images.adjustBrightness(icon, 0.8f));
    Buttons.showOnlyIcon(button);
    return button;
  }


  public static JButton createImageButton(Icon icon, int[] states) {

    return Buttons.createImageButton(icon, states, false);
  }


  public static JButton createImageButton(Icon icon, int[] states,
      boolean separateMask) {

    JButton button = Buttons.createButton();
    Buttons.setIcons(button, icon, states, separateMask);

    Buttons.showOnlyIcon(button);

    return button;
  }


  public static JButton createImageButton(Image im, int[] states) {

    return Buttons.createImageButton(im, states, false);
  }


  public static JButton createImageButton(Image im, int[] states,
      boolean separateMask) {

    JButton button = Buttons.createButton();
    Buttons.setIcons(button, im, states, separateMask);

    Buttons.showOnlyIcon(button);

    return button;
  }


  public static void setIcons(AbstractButton button, Icon icon, int[] states,
      boolean separateMask) {

    Buttons.setIcons(button, Images.getImage(icon), states, separateMask);
  }


  public static void setIcons(AbstractButton button, Image image, int[] states,
            boolean separateMask) {

    Image[] tiles = Images.split(image, states.length, separateMask);

    Buttons.setIcons(button, tiles, states);
  }


  public static void setIcons(AbstractButton button, Image[] images,
      int[] states) {

    for (int i = 0; i < states.length; i++) {
      Icon icon = new ImageIcon(images[i]);
      switch (states[i]) {
        case NORMAL:
          button.setIcon(icon);
          break;
        case DISABLED:
          button.setDisabledIcon(icon);
          break;
        case PRESSED:
          button.setPressedIcon(icon);
          break;
        case SELECTED:
          button.setSelectedIcon(icon);
          break;
        case ROLLOVER:
          button.setRolloverIcon(icon);
          break;
        case ROLLOVER_SELECTED:
          button.setRolloverSelectedIcon(icon);
          break;
        case DISABLED_SELECTED:
          button.setDisabledSelectedIcon(icon);
          break;
        default:
          // unknown state. Ignore.
          break;
      }
    }
  }


  public static JButton createHelpButton() {

    return Buttons.createHelpButton(null);
  }


  public static JButton createHelpButton(final ActionListener action) {

    JButton helpButton;

    boolean systemLAF = UIManager.getLookAndFeel().isNativeLookAndFeel();

    if (systemLAF && Platform.isMac()) {
      helpButton =
        Buttons.createImageButton(new ImageIcon(
              ClassLoader
                .getSystemResource("com/clt/resources/Help_MacOS_X.gif")));
    }
    else if (systemLAF && Platform.isWindows()) {
      helpButton =
        Buttons.createImageButton(new ImageIcon(
              ClassLoader
                .getSystemResource("com/clt/resources/Help_Windows.gif")));
    }
    else {
      helpButton =
        Buttons
          .createImageButton(new ImageIcon(
              ClassLoader.getSystemResource("com/clt/resources/Help_Metal.gif")));
    }

    if (action != null) {
      helpButton.addActionListener(action);
    }

    return helpButton;
  }
}
