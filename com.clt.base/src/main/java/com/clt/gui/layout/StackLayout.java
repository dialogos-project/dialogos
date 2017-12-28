/*
 * @(#)StackLayout.java
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

package com.clt.gui.layout;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.awt.Rectangle;
import java.util.LinkedList;
import java.util.List;

/**
 * @author dabo
 * 
 */

public class StackLayout
    implements LayoutManager2 {

  public static final String BOTTOM = "bottom";
  public static final String TOP = "top";

  private List<Component> components = new LinkedList<Component>();


  public void addLayoutComponent(Component comp, Object constraints) {

    synchronized (comp.getTreeLock()) {
      if (StackLayout.BOTTOM.equals(constraints)) {
        this.components.add(0, comp);
      }
      else if (StackLayout.TOP.equals(constraints)) {
        this.components.add(comp);
      }
      else {
        this.components.add(comp);
      }
    }
  }


  public void addLayoutComponent(String name, Component comp) {

    this.addLayoutComponent(comp, StackLayout.TOP);
  }


  public void removeLayoutComponent(Component comp) {

    synchronized (comp.getTreeLock()) {
      this.components.remove(comp);
    }
  }


  public float getLayoutAlignmentX(Container target) {

    return 0.5f;
  }


  public float getLayoutAlignmentY(Container target) {

    return 0.5f;
  }


  public void invalidateLayout(Container target) {

  }


  public Dimension preferredLayoutSize(Container parent) {

    synchronized (parent.getTreeLock()) {
      int width = 0;
      int height = 0;

      for (Component comp : this.components) {
        Dimension size = comp.getPreferredSize();
        width = Math.max(size.width, width);
        height = Math.max(size.height, height);
      }

      Insets insets = parent.getInsets();
      width += insets.left + insets.right;
      height += insets.top + insets.bottom;

      return new Dimension(width, height);
    }
  }


  public Dimension minimumLayoutSize(Container parent) {

    synchronized (parent.getTreeLock()) {
      int width = 0;
      int height = 0;

      for (Component comp : this.components) {
        Dimension size = comp.getMinimumSize();
        width = Math.max(size.width, width);
        height = Math.max(size.height, height);
      }

      Insets insets = parent.getInsets();
      width += insets.left + insets.right;
      height += insets.top + insets.bottom;

      return new Dimension(width, height);
    }
  }


  public Dimension maximumLayoutSize(Container target) {

    return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
  }


  public void layoutContainer(Container parent) {

    synchronized (parent.getTreeLock()) {
      int width = parent.getWidth();
      int height = parent.getHeight();

      Rectangle bounds = new Rectangle(0, 0, width, height);

      int componentsCount = this.components.size();

      for (int i = 0; i < componentsCount; i++) {
        Component comp = this.components.get(i);
        comp.setBounds(bounds);
        parent.setComponentZOrder(comp, componentsCount - i - 1);
      }
    }
  }
}