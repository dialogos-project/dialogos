/*
 * @(#)PSpinner.java
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

import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.properties.Property;

/**
 * @author dabo
 * 
 */
public class PSpinner<T>
    extends JSpinner {

  private Property<T> property;
  private boolean updating = false;

  private T[] values;


  public PSpinner(Property<T> property) {

    this.property = property;
    this.values = property.getPossibleValues();
  }

  ChangeListener l = new ChangeListener() {

    public void stateChanged(ChangeEvent evt) {

      if (!PSpinner.this.updating) {
        PSpinner.this.updating = true;
        if (PSpinner.this.values != PSpinner.this.property.getPossibleValues()) {
          PSpinner.this.initOptions();
        }
        else {
          PSpinner.this.setValue(PSpinner.this.property.getValueAsObject());
        }
        PSpinner.this.setEnabled(PSpinner.this.property.isEditable());
        PSpinner.this.updating = false;
      }
    }
  };

  ChangeListener al = new ChangeListener() {

    @SuppressWarnings("unchecked")
    public void stateChanged(ChangeEvent evt)
        {

          if (!PSpinner.this.updating) {
            PSpinner.this.updating = true;
            PSpinner.this.property.setValue((T)PSpinner.this.getValue());
            PSpinner.this.updating = false;
          }
        }
  };


  @Override
  public void addNotify() {

    super.addNotify();

    this.initOptions();

    this.property.addChangeListener(this.l);
    this.l.stateChanged(new ChangeEvent(this.property));
    this.addChangeListener(this.al);
  }


  @Override
  public void removeNotify() {

    this.removeChangeListener(this.al);
    this.property.removeChangeListener(this.l);
    super.removeNotify();
  }


  private void initOptions() {

    this.values = this.property.getPossibleValues();
    if (this.values == null) {
      this.setModel(new SpinnerListModel(new Object[0]));
    }
    else {
      this.setModel(new SpinnerListModel(this.values));
    }
    this.setValue(this.property.getValueAsObject());
    this.setEnabled(this.property.isEditable());
  }

}
