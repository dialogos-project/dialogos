/*
 * @(#)TextPane.java
 * Created on Thu Dec 12 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;

import com.clt.gui.GUI;
import com.clt.gui.LoggingPane;

/**
 * @author Daniel Bobbert
 * @version 6.0
 */

public class TextPane
    extends JPanel {

  private static final int SCROLLBACK_BUFFER_SIZE = 250;

  private LoggingPane text;
  private JLabel stateLabel;
  private GUIClient client;

  private PropertyChangeListener pcl = new PropertyChangeListener() {

    public void propertyChange(PropertyChangeEvent evt) {

      if (evt.getPropertyName().equals(GUIClient.PROPERTY_STATE)) {
        TextPane.this.adjustState();
      }
      else if (evt.getPropertyName().equals(GUIClient.PROPERTY_PORT)) {
        TextPane.this.adjustState();
      }
    }
  };


  public TextPane(GUIClient client) {

    super();

    this.client = client;

    this.setLayout(new GridBagLayout());

    this.text = new LoggingPane(TextPane.SCROLLBACK_BUFFER_SIZE);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.BOTH;

    gbc.gridx = gbc.gridy = 0;
    gbc.weightx = 0.0;
    gbc.weighty = 0.0;

    if (client != null) {
      this.stateLabel = new JLabel();
      this.add(new JLabel("State: "), gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      this.add(this.stateLabel, gbc);
      gbc.gridy++;
    }

    gbc.gridx = 0;
    gbc.gridwidth = 2;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;

    JScrollPane scrollPane =
      GUI.createScrollPane(this.text,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    this.add(scrollPane, gbc);

    this.adjustTitle();
    this.adjustState();

    this.setPreferredSize(new Dimension(250, 300));
  }


  @Override
  public void addNotify() {

    super.addNotify();
    if (this.client != null) {
      this.client.addPropertyChangeListener(this.pcl);
    }

    this.adjustTitle();
    this.adjustState();
  }


  @Override
  public void removeNotify() {

    if (this.client != null) {
      this.client.removePropertyChangeListener(this.pcl);
    }
    super.removeNotify();
  }


  private void adjustState() {

    // System.out.println("Client " + client.getState() + " on port: " +
    // client.getPort());

    if (this.client != null) {
      switch (this.client.getState()) {
        case CONNECTING:
          this.stateLabel.setText("Listening on port " + this.client.getPort()
            + "...");
          break;
        case CONNECTED:
          this.stateLabel.setText("Connected (port " + this.client.getPort()
            + ").");
          this.clear();
          break;
        case DISCONNECTED:
          this.stateLabel.setText("Disconnected.");
          break;
        default:
          this.stateLabel.setText("Unknown state");
          break;
      }
      this.stateLabel.repaint();
    }
  }


  private void adjustTitle() {

    if (this.client != null) {
      this.setBorder(BorderFactory.createTitledBorder(new EtchedBorder(
        EtchedBorder.LOWERED),
              this.client.getName() + " (v" + this.client.getVersion() + ")"));
    }
  }


  public void setDisplayFont(Font font) {

    this.text.setFont(font);
  }


  public synchronized void print(String s) {

    this.text.print(s);
  }


  public synchronized void print(String s, Color c) {

    this.text.print(s, c);
  }


  public synchronized void println() {

    this.text.println();
  }


  public synchronized void println(String s) {

    this.text.println(s);
  }


  public synchronized void println(String s, Color c) {

    this.text.println(s, c);
  }


  public void clear() {

    this.text.clear();
  }


  public void flush() {

    this.paintImmediately(0, 0, this.getWidth(), this.getHeight());
  }
}