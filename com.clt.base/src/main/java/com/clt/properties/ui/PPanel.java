/*
 * @(#)PPanel.java
 * Created on 20.07.2006 by dabo
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

package com.clt.properties.ui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 * @author dabo
 * 
 */
public class PPanel
    extends JPanel {

  public PPanel(LayoutManager layout) {

    super(layout);
  }


  @Override
  public void setEnabled(boolean enabled) {

    super.setEnabled(enabled);
    for (int i = 0; i < this.getComponentCount(); i++) {
      this.getComponent(i).setEnabled(enabled);
    }
  }
}
