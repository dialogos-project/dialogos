/*
 * @(#)BooleanProperty.java
 * Created on 16.03.05
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

package com.clt.properties;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.gui.menus.CmdCheckBoxMenuItem;
import com.clt.gui.menus.MenuCommander;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class BooleanProperty
    extends Property<Boolean> {

  private static final Boolean[] values = { Boolean.TRUE, Boolean.FALSE };


  public BooleanProperty(String id) {

    super(id, Property.EDIT_TYPE_CHECKBOX);
  }


  public abstract boolean getValue();


  protected abstract void setValueImpl(boolean value);


  public final void setValue(boolean value) {

    if (value != this.getValue()) {
      this.setValueImpl(value);
      this.fireChange();
    }
  }


  @Override
  public void setValueFromString(String value) {

    this.setValue((value != null) && value.equals("true"));
  }


  @Override
  public String getValueAsString() {

    return String.valueOf(this.getValue());
  }


  @Override
  public Boolean[] getPossibleValues() {

    return BooleanProperty.values;
  }


  @Override
  public void setValue(Boolean o) {

    this.setValue(Boolean.TRUE.equals(o));
  }


  @Override
  public Boolean getValueAsObject() {

    return this.getValue() ? Boolean.TRUE : Boolean.FALSE;
  }


  @Override
  protected int getSupportedEditTypesImpl() {

    return Property.EDIT_TYPE_CHECKBOX;
  }


  @Override
  protected JComponent createEditorComponent(int editType, boolean label) {

    final JCheckBox b = new JCheckBox(label ? this.getName() : null) {

      ChangeListener l = new ChangeListener() {

        public void stateChanged(ChangeEvent evt) {

          setSelected(BooleanProperty.this.getValue());
          setEnabled(BooleanProperty.this.isEditable());
        }
      };


      @Override
      public void addNotify() {

        super.addNotify();
        BooleanProperty.this.addChangeListener(this.l);
        this.l.stateChanged(new ChangeEvent(BooleanProperty.this));
      }


      @Override
      public void removeNotify() {

        BooleanProperty.this.removeChangeListener(this.l);
        super.removeNotify();
      }
    };
    b.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent evt) {

        BooleanProperty.this.setValue(b.isSelected());
      }
    });
    return b;
  }


  @Override
  public JMenuItem createMenuItem() {

    return new CmdCheckBoxMenuItem(this.getName(), 1, null,
      new MenuCommander() {

        public String menuItemName(int cmd, String oldName) {

          if (BooleanProperty.this.isEditable()) {
            return BooleanProperty.this.getName();
          }
          else {
            return null;
          }
        }


        public boolean menuItemState(int cmd) {

          return BooleanProperty.this.getValue();
        }


        public boolean doCommand(int cmd) {

          BooleanProperty.this.setValue(!BooleanProperty.this.getValue());
          return true;
        }

      });
  }
}