/*
 * @(#)SearchResult.java
 * Created on Tue Jul 19 2005
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

package com.clt.diamant.graph.search;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.clt.diamant.graph.ui.GraphUI;
import com.clt.gui.GUI;
import com.clt.gui.Images;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class SearchResult
    extends JPanel {

  public enum Type {
        INFO, WARNING, ERROR
  }

  private final Color lightBlue = new Color(250, 250, 255);

  private Icon badge;
  private String message;
  private Type type;


  public SearchResult(Icon badge, String message, Type type) {

    this.message = message;
    this.type = type;
    this.badge = badge;

    this.setOpaque(true);
    this.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
  }


  @Override
  public void addNotify() {

    super.addNotify();

    this.removeAll();
    this.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.gridheight = 2;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.NORTH;
    gbc.weightx = 0.0;
    gbc.insets = new Insets(1, 2, 1, 2);

    if (this.type == Type.WARNING) {
      this.add(new JLabel(Images.load("Warning16.png")), gbc);
    }
    else if (this.type == Type.ERROR) {
      this.add(new JLabel(Images.load("Error16.png")), gbc);
    }
    else {
      if (this.badge != null) {
        this.add(new JLabel(this.badge), gbc);
      }
      else {
        this.add(new JLabel(Images.load("Info16.png")), gbc);
      }
    }

    gbc.gridx++;
    gbc.gridheight = 1;

    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weightx = 0.0;

    JLabel docName = new JLabel(this.getDocumentName() + ":");
    docName.setFont(GUI.getSmallSystemFont().deriveFont(Font.BOLD));
    this.add(docName, gbc);
    gbc.gridx++;
    gbc.weightx = 1.0;
    // gbc.insets = new Insets(0, 16, 0, 0);
    JLabel source = new JLabel(this.getSource());
    source.setFont(GUI.getSmallSystemFont().deriveFont(Font.ITALIC));
    this.add(source, gbc);

    gbc.gridx--;
    gbc.gridy++;
    gbc.gridwidth = 2;
    JLabel msg = new JLabel(this.message);
    msg.setFont(GUI.getSmallSystemFont());
    this.add(msg, gbc);
  }


  public Type getType() {

    return this.type;
  }


  public abstract String getDocumentName();


  public abstract String getSource();


  public abstract GraphUI showResult(JComponent parent);


  public abstract boolean isRelevant();


  public void setSelected(boolean selected, boolean even) {

    Color foregroundColor, backgroundColor;
    if (selected) {
      foregroundColor = Color.white;
      backgroundColor = Color.gray;
    }
    else {
      foregroundColor = Color.black;
      if (even) {
        backgroundColor = Color.white;
      }
      else {
        backgroundColor = this.lightBlue;
      }
    }

    this.setForeground(foregroundColor);
    this.setBackground(backgroundColor);
    for (int i = this.getComponentCount() - 1; i >= 0; i--) {
      Component c = this.getComponent(i);
      c.setForeground(foregroundColor);
      c.setBackground(backgroundColor);
    }
  }
}