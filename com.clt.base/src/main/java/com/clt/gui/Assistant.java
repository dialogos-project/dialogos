/*
 * @(#)Assistant.java
 * Created on Tue Sep 16 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Assistant {

  private String title;

  private Map<String, AssistantPanel> panels;

  private JPanel content, cards;

  private JButton back_button, cancel_button, continue_button;

  private transient Stack<String> history = new Stack<String>();

  private transient String currentCard = null;

  private transient boolean success = false;


  public Assistant(String title) {

    this(title, (JComponent)null);
  }


  public Assistant(String title, String logo) {

    this(title, Assistant.createLogo(logo));
  }


  public Assistant(String title, Image logo) {

    this(title, new JLabel(new ImageIcon(logo), SwingConstants.CENTER));
  }


  public Assistant(String title, Icon logo) {

    this(title, new JLabel(logo, SwingConstants.CENTER));
  }


  public Assistant(String title, JComponent logo) {

    this.title = title;

    this.panels = new LinkedHashMap<String, AssistantPanel>();

    this.cards = new JPanel(new CardLayout());
    this.cards.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    int width = 0;
    this.back_button = new JButton(GUI.getString("Back"));
    width = Math.max(width, this.back_button.getPreferredSize().width);
    this.continue_button = new JButton(GUI.getString("Finish"));
    width = Math.max(width, this.continue_button.getPreferredSize().width);
    this.continue_button.setText(GUI.getString("Continue"));
    width = Math.max(width, this.continue_button.getPreferredSize().width);
    this.cancel_button = new JButton(GUI.getString("Cancel"));

    this.back_button.setPreferredSize(new Dimension(width, this.back_button
      .getPreferredSize().height));
    this.continue_button.setPreferredSize(new Dimension(width,
            this.continue_button.getPreferredSize().height));

    JPanel linePanel = new JPanel(new BorderLayout());

    linePanel
      .add(new JSeparator(SwingConstants.HORIZONTAL), BorderLayout.NORTH);

    Box buttonBox = Box.createHorizontalBox();
    buttonBox.add(Box.createHorizontalGlue());
    buttonBox.add(this.back_button);
    buttonBox.add(this.continue_button);
    buttonBox.add(Box.createHorizontalStrut(12));
    buttonBox.add(this.cancel_button);

    JPanel buttonPanel = new JPanel(new GridLayout(1, 1));
    buttonPanel.add(buttonBox);
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    linePanel.add(buttonPanel, BorderLayout.EAST);

    JPanel picture = new JPanel(new BorderLayout());
    picture.setBackground(Color.white);
    final Icon image = new ImageIcon(
            ClassLoader.getSystemResource("com/clt/resources/Assistant.gif"));
    JPanel p = new JPanel(new BorderLayout()) {

      @Override
      protected void paintComponent(Graphics g) {

        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        image.paintIcon(this, g, (this.getWidth() - image.getIconWidth()) / 2,
          this.getHeight()
                        - image.getIconHeight());
      }


      @Override
      public Dimension getPreferredSize() {

        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, image.getIconWidth());
        d.height = Math.max(d.height, image.getIconHeight());
        return d;
      }


      @Override
      public boolean isOpaque() {

        return true;
      }
    };
    if (logo != null) {
      JPanel logoPanel = new JPanel(new BorderLayout());
      logoPanel.setOpaque(false);
      logoPanel.add(logo, BorderLayout.CENTER);
      logoPanel.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 6));
      p.add(logoPanel, BorderLayout.NORTH);
    }
    picture.add(p, BorderLayout.CENTER);
    // picture.add(new JLabel(image), BorderLayout.CENTER);
    picture.add(new JSeparator(SwingConstants.VERTICAL), BorderLayout.EAST);

    this.content = new JPanel();
    this.content.setLayout(new BorderLayout());
    this.content.add(linePanel, BorderLayout.SOUTH);
    this.content.add(picture, BorderLayout.WEST);
    this.content.add(this.cards, BorderLayout.CENTER);

  }


  public void add(AssistantPanel panel) {

    this.panels.put(panel.getId(), panel);
    this.cards.add(panel, panel.getId());
  }


  private void showCard(String name) {

    if (this.currentCard != null) {
      this.history.push(this.currentCard);
    }
    this.currentCard = name;
    ((CardLayout)this.cards.getLayout()).show(this.cards, this.currentCard);
    this.updateButtons();
  }


  private void back() {

    this.currentCard = this.history.pop();
    ((CardLayout)this.cards.getLayout()).show(this.cards, this.currentCard);
    this.updateButtons();
  }


  private void updateButtons() {

    this.back_button.setEnabled(this.history.size() > 0);
    if (this.currentCard == null) {
      this.continue_button.setEnabled(false);
      this.continue_button.setText(GUI.getString("Continue"));
    }
    else {
      AssistantPanel panel = this.panels.get(this.currentCard);
      this.continue_button.setEnabled(panel.isComplete());
      this.continue_button.setText(GUI.getString(panel.getNextPanel() == null
        ? "Finish"
                    : "Continue"));
    }
    this.cancel_button.setEnabled(true);
  }


  public synchronized boolean show(Component parent, String startPanel) {

    final JDialog dialog =
      new JDialog(JOptionPane.getFrameForComponent(parent), this.title, true);

    this.currentCard = null;
    this.history.clear();
    this.success = false;

    ActionListener continueHandler = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        String next =
          Assistant.this.panels.get(Assistant.this.currentCard).getNextPanel();
        if (next == null) {
          Assistant.this.success = true;
          dialog.dispose();
        }
        else {
          Assistant.this.showCard(next);
        }
      }
    };

    ActionListener backHandler = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        if (Assistant.this.history.size() > 0) {
          Assistant.this.back();
        }
      }
    };

    ActionListener cancelHandler = new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        Assistant.this.success = false;
        dialog.dispose();
      }
    };

    ChangeListener changeListener = new ChangeListener() {

      public void stateChanged(ChangeEvent evt) {

        Assistant.this.updateButtons();
      }
    };

    for (AssistantPanel panel : this.panels.values()) {
      panel.addEditStateListener(changeListener);
    }

    this.continue_button.addActionListener(continueHandler);
    this.back_button.addActionListener(backHandler);
    this.cancel_button.addActionListener(cancelHandler);

    if (startPanel != null) {
      this.showCard(startPanel);
    }
    else if (!this.panels.isEmpty()) {
      this.showCard(this.panels.keySet().iterator().next());
    }
    this.updateButtons();

    Dimension d = this.content.getPreferredSize();
    d.width = Math.max(d.width, d.height * 5 / 3);
    this.content.setPreferredSize(d);

    dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    dialog.setContentPane(this.content);
    dialog.getRootPane().setDefaultButton(this.continue_button);
    dialog.pack();
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

    this.continue_button.removeActionListener(continueHandler);
    this.back_button.removeActionListener(backHandler);
    this.cancel_button.removeActionListener(cancelHandler);

    for (AssistantPanel panel : this.panels.values()) {
      panel.removeEditStateListener(changeListener);
    }

    return this.success;
  }


  private static JComponent createLogo(String text) {

    StaticText l = new StaticText(text);
    l.setAlignment(StaticText.CENTER);
    l.setFont(new Font("SansSerif", Font.BOLD, 21));
    return l;
  }
}