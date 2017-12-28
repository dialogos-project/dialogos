/*
 * @(#)PopupPane.java
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

/**
 * A PopupPane is very similar to a JTabbedPane. It contains a number of panes,
 * of which only one is visible at a time. The main difference to JTabbedPane
 * is, that instead of a row of tabs a combobox is used to select the currently
 * visible tab.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class PopupPane
    extends JPanel {

  private JComboBox pageMenu;
  private JComponent groupTitle;
  private JPanel content;
  private CardLayout layout;
  private Border groupBorder;


  public PopupPane() {

    this(null);
  }


  public PopupPane(String title) {

    this.groupBorder = new EtchedBorder(EtchedBorder.LOWERED);

    this.setLayout(new GridBagLayout());

    this.pageMenu = new JComboBox() {

      @Override
      public Dimension getPreferredSize() {

        Dimension d = super.getPreferredSize();
        d = new Dimension(Math.max(d.width, 80), d.height);
        return d;
      }


      @Override
      public Dimension getMinimumSize() {

        return new Dimension(super.getMinimumSize().width, super
          .getPreferredSize().height);
      }
    };
    this.pageMenu.setEditable(false);

    this.pageMenu
      .setRenderer(new ComponentRenderer(this.pageMenu.getRenderer()));

    this.pageMenu.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        Page page = (Page)PopupPane.this.pageMenu.getSelectedItem();
        if (page != null) {
          PopupPane.this.layout.show(PopupPane.this.content, page.getTitle());
        }
      }
    });

    this.layout = new CardLayout();
    this.content = new JPanel(this.layout);

    GridBagConstraints gbc = new GridBagConstraints();

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 3, 0, 3);
    gbc.fill = GridBagConstraints.NONE;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;
    gbc.anchor = GridBagConstraints.CENTER;

    this.groupTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    if (title != null) {
      this.groupTitle.add(new JLabel(title));
    }
    this.groupTitle.add(this.pageMenu);

    this.addImpl(this.groupTitle, gbc, 0);

    gbc.gridy++;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
    gbc.insets = new Insets(6, 6, 4, 6);
    gbc.fill = GridBagConstraints.BOTH;
    this.addImpl(this.content, gbc, 1);
  }


  public void addTab(String title, Component component) {

    this.addTab(title, null, component);
  }


  public void addTab(String title, Icon icon, Component component) {

    this.addTab(title, icon, component, null);
  }


  public void addTab(String title, Icon icon, Component component, String tip) {

    this.insertTab(title, icon, component, tip, this.pageMenu.getItemCount());
  }


  public void insertTab(String title, Icon icon, Component component,
      String tip, int index) {

    Page page = new Page(title, icon, component);
    this.pageMenu.insertItemAt(page, index);

    this.content.add(component, title);
    this.setSelectedIndex(this.pageMenu.getItemCount() - 1);
  }


  @Override
  public Component add(Component component, int index) {

    // Container.add() interprets -1 as "append", so convert
    // the index appropriately to be handled by the vector
    this.insertTab(component.getName(), null, component, null, index == -1
      ? this.getTabCount() : index);
    return component;
  }


  @Override
  public void add(Component component, Object constraints, int index) {

    Icon icon = constraints instanceof Icon ? (Icon)constraints : null;
    String title = constraints instanceof String ? (String)constraints : null;
    // Container.add() interprets -1 as "append", so convert
    // the index appropriately to be handled by the vector
    this.insertTab(title, icon, component, null, index == -1 ? this
      .getTabCount() : index);
  }


  @Override
  public Component add(Component component) {

    this.addTab(component.getName(), component);
    return component;
  }


  @Override
  public Component add(String title, Component component) {

    this.addTab(title, component);
    return component;
  }


  @Override
  public void add(Component component, Object constraints) {

    if (constraints instanceof String) {
      this.addTab((String)constraints, component);
    }
    else if (constraints instanceof Icon) {
      this.addTab(null, (Icon)constraints, component);
    }
    else {
      this.add(component);
    }
  }


  public int getSelectedIndex() {

    return this.pageMenu.getSelectedIndex();
  }


  public void setSelectedIndex(int index) {

    this.pageMenu.setSelectedIndex(index);
  }


  public Component getSelectedComponent() {

    Page page = (Page)this.pageMenu.getSelectedItem();
    if (page != null) {
      return page.getComponent();
    }
    else {
      return null;
    }
  }


  public void setSelectedComponent(Component c) {

    for (int i = 0; i < this.pageMenu.getItemCount(); i++) {
      Page page = (Page)this.pageMenu.getItemAt(i);
      if (page.getComponent() == c) {
        this.pageMenu.setSelectedIndex(i);
        break;
      }
    }
  }


  public String getTitleAt(int index) {

    return ((Page)this.pageMenu.getItemAt(index)).getTitle();
  }


  public Icon getIconAt(int index) {

    return ((Page)this.pageMenu.getItemAt(index)).getIcon();
  }


  public Icon getDisabledIconAt(int index) {

    return ((Page)this.pageMenu.getItemAt(index)).getDisabledIcon();
  }


  public int getTabCount() {

    return this.pageMenu.getItemCount();
  }


  @Override
  public void removeAll() {

    this.content.removeAll();
    this.pageMenu.setModel(new DefaultComboBoxModel());
  }


  @Override
  protected void paintComponent(Graphics g) {

    super.paintComponent(g);

    int height = this.groupTitle.getHeight();
    this.groupBorder.paintBorder(this, g, 0, height / 2, this.getWidth(), this
      .getHeight()
      - height / 2);
  }

  private class Page
        extends JMenuItem {

    Component component;


    public Page(String title, Icon icon, Component component) {

      super(title, icon);
      this.component = component;
      this.setBorder(null);
    }


    @Override
    public Component getComponent() {

      return this.component;
    }


    public String getTitle() {

      return this.getText();
    }


    @Override
    public String toString() {

      return this.getTitle();
    }
  }
}