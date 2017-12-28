/*
 * @(#)PComboBox.java
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.properties.Property;

/**
 * @author dabo
 * 
 */
public class PComboBox<T>
    extends JComboBox {

  private Property<T> property;
  private boolean updating = false;

  private T[] values;


  public PComboBox(Property<T> property) {

    this.property = property;
    this.values = property.getPossibleValues();
  }

  ChangeListener l = new ChangeListener() {

    public void stateChanged(ChangeEvent evt) {

      if (!PComboBox.this.updating) {
        PComboBox.this.updating = true;
        if (PComboBox.this.values != PComboBox.this.property
          .getPossibleValues()) {
          PComboBox.this.initOptions();
        }
        else {
          PComboBox.this.setSelectedItem(PComboBox.this.property
            .getValueAsObject());
        }
        PComboBox.this.setEnabled(PComboBox.this.property.isEditable());
        PComboBox.this.updating = false;
      }
    }
  };

  ActionListener al = new ActionListener() {

    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent evt)
        {

          if (!PComboBox.this.updating) {
            PComboBox.this.updating = true;
            PComboBox.this.property.setValue((T)PComboBox.this
              .getSelectedItem());
            PComboBox.this.updating = false;
          }
        }
  };


  @Override
  public void addNotify() {

    super.addNotify();

    this.initOptions();

    this.property.addChangeListener(this.l);
    this.l.stateChanged(new ChangeEvent(this.property));
    this.addActionListener(this.al);
  }


  @Override
  public void removeNotify() {

    this.removeActionListener(this.al);
    this.property.removeChangeListener(this.l);
    super.removeNotify();
  }


  private void initOptions() {

    this.values = this.property.getPossibleValues();
    if (this.values == null) {
      this.setModel(new DefaultComboBoxModel(new Object[0]));
    }
    else {
      this.setModel(new DefaultComboBoxModel(this.values));
    }
    this.setSelectedItem(this.property.getValueAsObject());
    this.setEnabled(this.property.isEditable());
  }


  @Override
  public Dimension getPreferredSize() {

    // force a minimum width of 100 pixels
    Dimension d = super.getPreferredSize();
    return new Dimension(Math.max(d.width, 100), d.height);
  }


  @Override
  public Dimension getMinimumSize() {

    // force a minimum width of 100 pixels
    Dimension d = super.getMinimumSize();
    return new Dimension(Math.max(d.width, 100), d.height);
  }
}
