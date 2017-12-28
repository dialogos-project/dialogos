/*
 * @(#)DisclosurePanel.java
 * Created on 10.04.2006 by dabo
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author dabo
 */
public class DisclosurePanel extends JPanel {

  private boolean inited = false;
  private boolean resizeWindow = true;


  /** Wrap <code>content</code> in a disclosable view. */
  public DisclosurePanel(JComponent content) {

    this(content, null);
  }


  /** Wrap <code>content</code> in a named disclosable view. */
  public DisclosurePanel(final JComponent content, String title) {

    super(new GridBagLayout());

    final ImageIcon disclosureOpened = Images.loadBuiltin("DisclosureDown.png");
    final ImageIcon disclosureClosed =
      Images.loadBuiltin("DisclosureRight.png");

    final JLabel disclosureIcon =
      new JLabel(title, content.isVisible() ? disclosureOpened
                : disclosureClosed, SwingConstants.LEFT);
    content.addComponentListener(new ComponentAdapter() {

      @Override
      public void componentShown(ComponentEvent evt) {

        if (evt.getComponent() == content) {
          disclosureIcon.setIcon(disclosureOpened);
        }
      }


      @Override
      public void componentHidden(ComponentEvent evt) {

        if (evt.getComponent() == content) {
          disclosureIcon.setIcon(disclosureClosed);
        }
      }
    });
    disclosureIcon.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent evt) {

        Dimension oldSize = null;
        Window w = null;

        if (DisclosurePanel.this.resizeWindow) {
          w = GUI.getWindowForComponent(content);
          if (w != null) {
            oldSize = w.getPreferredSize();
          }
        }
        content.setVisible(!content.isVisible());
        if (oldSize != null) {
          Dimension newSize = w.getPreferredSize();
          w.setSize(w.getWidth() + newSize.width - oldSize.width,
                              w.getHeight() + newSize.height - oldSize.height);
          w.validate();
        }
        else {
          DisclosurePanel.this.revalidate();
        }
      }
    });

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(0, 0, 3, 0);
    this.add(disclosureIcon, gbc);

    gbc.gridy++;
    gbc.weighty = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.insets = new Insets(0, 0, 0, 0);
    this.add(content, gbc);

    this.inited = true;
  }


  public void setResizeWindow(boolean resizeWindow) {

    this.resizeWindow = resizeWindow;
  }


  @Override
  protected void addImpl(Component comp, Object constraints, int index) {

    if (this.inited) {
      throw new UnsupportedOperationException(
              "You cannot manually add components to a DisclosurePanel.");
    }
    super.addImpl(comp, constraints, index);
  }

}
