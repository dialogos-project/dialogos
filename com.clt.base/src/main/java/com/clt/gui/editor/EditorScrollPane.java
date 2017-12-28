/*
 * @(#)EditorScrollPane.java
 * Created on 04.04.2007 by dabo
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

package com.clt.gui.editor;

import java.awt.Color;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.JTextComponent;

import com.clt.gui.border.LinesBorder;

/**
 * @author dabo
 * 
 */
public class EditorScrollPane
    extends JScrollPane {

  private JTextComponent text;
  private boolean showLineNumbers = true;


  public EditorScrollPane(JTextComponent text) {

    super(text, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    this.text = text;

    this.init();
  }


  private void init() {

    if (this.showLineNumbers) {
      LineNumbers ln = new LineNumbers(this.text);
      ln.setForeground(Color.DARK_GRAY);
      ln.setBorder(new LinesBorder("r", Color.LIGHT_GRAY));
      this.setRowHeaderView(ln);
    }
    else {
      this.setRowHeader(null);
    }
  }


  public boolean getShowLineNumbers() {

    return this.showLineNumbers;
  }


  public void setShowLineNumbers(boolean showLineNumbers) {

    if (this.showLineNumbers != showLineNumbers) {
      this.showLineNumbers = showLineNumbers;
      this.init();
    }
  }
}
