/*
 * @(#)GUIClientStartupScreen.java
 * Created on 29.05.2006 by dabo
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

package com.clt.dialog.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import com.clt.event.ProgressEvent;
import com.clt.gui.AboutDialog;
import com.clt.gui.WindowUtils;
import com.clt.util.Wrapper;

/**
 * @author dabo
 * 
 */
public class GUIClientStartupScreen
    extends JFrame {

  private final boolean smoothProgress = true;

  private int numClients;
  private JLabel clientStatus;
  private JProgressBar clientProgress;
  private JLabel status;
  private JProgressBar progress;
  private JPanel progressPanel;
  private JComponent header;
  private JPanel main;
  private int currentClient;


  public GUIClientStartupScreen(String title, String version) {

    super(title + " " + version);

    this.numClients = 0;

    this.progressPanel = new JPanel(new GridBagLayout());
    this.progressPanel.setOpaque(false);

    GridBagConstraints gbc = new GridBagConstraints();
    this.clientStatus = new JLabel("Initializing " + title + "...");
    this.clientProgress = new JProgressBar(SwingConstants.HORIZONTAL, 0, 300);
    this.status = new JLabel("Initializing clients...");
    this.progress =
      new JProgressBar(SwingConstants.HORIZONTAL, 0,
            this.smoothProgress ? this.clientProgress.getMaximum()
              : this.numClients);
    this.clientProgress.setPreferredSize(new Dimension(this.clientProgress
      .getMaximum(),
            this.clientProgress.getPreferredSize().height));

    this.header = AboutDialog.createHeader(title, version);

    gbc.gridx = gbc.gridy = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 10, 5, 10);

    this.progressPanel.add(this.status, gbc);
    gbc.gridy++;
    this.progressPanel.add(this.progress, gbc);
    gbc.gridy++;

    this.progressPanel.add(this.clientStatus, gbc);
    gbc.gridy++;
    this.progressPanel.add(this.clientProgress, gbc);

    this.currentClient = 0;
    this.main = new JPanel(new GridLayout(1, 1));
    this.main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
  }


  public void show(Component content) {

    if (content == null) {
      JPanel main = new JPanel(new BorderLayout());
      main.setOpaque(false);
      main.add(this.progressPanel, BorderLayout.CENTER);
      main.add(Box
        .createVerticalStrut(this.header.getPreferredSize().height / 2),
                BorderLayout.SOUTH);
      content = main;
    }

    this.main.removeAll();
    this.main.add(content);

    if (!this.isVisible()) {
      this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
      this.setResizable(false);

      JPanel p = new JPanel(new BorderLayout());
      p.add(this.header, BorderLayout.NORTH);
      p.add(AboutDialog.createStripes("left"), BorderLayout.WEST);
      p.add(AboutDialog.createStripes("right"), BorderLayout.EAST);
      p.add(this.main, BorderLayout.CENTER);
      this.setContentPane(p);
    }

    if (!this.isVisible()) {
      this.pack();
      WindowUtils.setLocationRelativeTo(this, null);
      this.setVisible(true);
    }
    else {
      this.validate();
      this.repaint();
    }
  }


  public String input(String title, Component body, String[] options,
      String defaultOption) {

    JPanel panel = new JPanel(new BorderLayout(6, 6));

    if (title != null) {
      panel.add(new JLabel(title), BorderLayout.NORTH);
    }

    if (body != null) {
      panel.add(body, BorderLayout.CENTER);
    }

    JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));

    final Object buttonLock = new Object();
    final Wrapper<String> result = new Wrapper<String>();
    for (final String option : options) {
      JButton button = new JButton(option);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          result.set(option);
          synchronized (buttonLock) {
            buttonLock.notifyAll();
          }
        }
      });
      if ((defaultOption != null) && option.equals(defaultOption)) {
        this.getRootPane().setDefaultButton(button);
      }
      buttons.add(button);
    }

    panel.add(buttons, BorderLayout.SOUTH);

    synchronized (buttonLock) {
      this.show(panel);
      try {
        buttonLock.wait();
      } catch (InterruptedException exn) {
        // ignore
      }
    }

    return result.get();
  }


  public void addNumClients(int numClients) {

    this.numClients += numClients;
    this.progress.setMaximum(this.smoothProgress ? this.clientProgress
      .getMaximum() : this.numClients);
  }


  public void setStatus(String status) {

    this.status.setText(status);
  }


  public void setClientStatus(String status) {

    this.clientStatus.setText(status);
  }


  public void setClientProgress(double value) {

    if (Double.isNaN(value) || (value < 0)) {
      this.clientProgress.setIndeterminate(true);
    }
    else {
      this.clientProgress.setIndeterminate(false);
      this.clientProgress
        .setValue(this.clientProgress.getMinimum()
                    + (int)(value * (this.clientProgress.getMaximum() - this.clientProgress
                      .getMinimum())));
      if (this.smoothProgress) {
        this.progress
          .setValue((this.progress.getMaximum() * this.currentClient + this.clientProgress
            .getValue())
                      / this.numClients);
      }
    }
  }


  public void increaseProgress(int clients) {

    this.currentClient += clients;
    this.progress.setValue(this.smoothProgress
      ? (this.progress.getMaximum() * this.currentClient) / this.numClients
                : this.currentClient);
  }


  public void setClientStatus(ProgressEvent evt) {

    this.setClientStatus(evt.getMessage());
    if (evt.getEnd() - evt.getStart() <= 0) {
      this.setClientProgress(Double.NaN);
    }
    else {
      this.setClientProgress((double)(evt.getCurrent() - evt.getStart())
                  / (double)(evt.getEnd() - evt.getStart()));
    }

    this.repaint();
  }
}
