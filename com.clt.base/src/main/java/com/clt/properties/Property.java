/*
 * @(#)Property.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.clt.gui.OptionPane;
import com.clt.gui.menus.CmdMenuItem;
import com.clt.gui.menus.MenuCommander;
import com.clt.properties.ui.PComboBox;
import com.clt.properties.ui.PList;
import com.clt.properties.ui.PPanel;
import com.clt.properties.ui.PSpinner;
import com.clt.properties.ui.PTextField;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class Property<T> {

  public static final int EDIT_TYPE_LIST = 1 << 0;
  public static final int EDIT_TYPE_COMBOBOX = 1 << 1;
  public static final int EDIT_TYPE_RADIOBUTTONS = 1 << 2;
  public static final int EDIT_TYPE_RADIOBUTTONS_VERTICAL = 1 << 3;
  public static final int EDIT_TYPE_RADIOBUTTONS_HORIZONTAL = 1 << 4;
  public static final int EDIT_TYPE_CHECKBOX = 1 << 5;
  public static final int EDIT_TYPE_NUMBERFIELD = 1 << 6;
  public static final int EDIT_TYPE_TEXTFIELD = 1 << 7;
  public static final int EDIT_TYPE_SLIDER = 1 << 8;
  public static final int EDIT_TYPE_SPINNER = 1 << 9;
  public static final int EDIT_TYPE_COLOR_CHOOSER = 1 << 10;
  public static final int EDIT_TYPE_FILE_CHOOSER = 1 << 11;

  private Collection<ChangeListener> changeListeners;

  private String id;

  private int editType;

  private boolean editable = true;


  public Property(String id, int editType) {

    this.id = id;
    this.editType = editType;
    this.changeListeners = new ArrayList<ChangeListener>();
  }


  public String getID() {

    return this.id;
  }


  public String getName() {

    return this.getID();
  }


  public abstract String getDescription();


  public abstract void setValueFromString(String value)
      throws java.text.ParseException;


  public abstract String getValueAsString();


  public abstract void setValue(T value);


  public abstract T getValueAsObject();


  /**
   * Return an array of possible values or <code>null</code>, if the property
   * doesn't have a finite set of values. Subclasses should always return the
   * same array object, because the default editors will reinitialize
   * themselves, whenever the returned array changes.
   */
  public abstract T[] getPossibleValues();


  public boolean isEditable() {

    return this.editable;
  }


  public void setEditable(boolean editable) {

    this.editable = editable;
    this.fireChange();
  }


  public void update() {

    this.fireChange();
  }


  public final void addChangeListener(ChangeListener l) {

    synchronized (this.changeListeners) {
      this.changeListeners.add(l);
    }
  }


  public final void removeChangeListener(ChangeListener l) {

    synchronized (this.changeListeners) {
      this.changeListeners.remove(l);
    }
  }


  protected final void fireChange() {

    synchronized (this.changeListeners) {
      ChangeEvent evt = new ChangeEvent(this);
      // make a copy of the listeners list in order to modify it from
      // within stateChanged, e.g. for removing and creating editors.
      for (ChangeListener l : new ArrayList<ChangeListener>(
        this.changeListeners)) {
        l.stateChanged(evt);
      }
    }
  }


  public int getEditType() {

    return this.editType;
  }


  public void setEditType(int editType) {

    if ((this.getSupportedEditTypes() & editType) == 0) {
      throw new IllegalArgumentException("Unsupported type of editor");
    }
    this.editType = editType;
  }


  public final JComponent createEditor() {

    return this.createEditor(true);
  }


  public final JComponent createEditor(boolean label) {

    return this.createEditor(this.getEditType(), label);
  }


  public final JComponent createEditor(int editType, boolean label) {

    if ((this.getSupportedEditTypes() & editType) == 0) {
      throw new IllegalArgumentException("Unsupported type of editor");
    }

    JComponent c = this.createEditorComponent(editType, label);
    c.setToolTipText(this.getDescription());
    return c;
  }


  protected final int getSupportedEditTypes() {

    int editTypes = this.getSupportedEditTypesImpl();
    if (this.getPossibleValues() != null) {
      editTypes |=
        Property.EDIT_TYPE_COMBOBOX | Property.EDIT_TYPE_LIST
          | Property.EDIT_TYPE_RADIOBUTTONS
                  | Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL
          | Property.EDIT_TYPE_RADIOBUTTONS_VERTICAL
                  | Property.EDIT_TYPE_SPINNER;
    }
    return editTypes;
  }


  protected abstract int getSupportedEditTypesImpl();


  protected JComponent createEditorComponent(int editType, boolean label) {

    switch (editType) {
      case EDIT_TYPE_RADIOBUTTONS:
      case EDIT_TYPE_RADIOBUTTONS_HORIZONTAL:
      case EDIT_TYPE_RADIOBUTTONS_VERTICAL:
        return this.createRadioButtons(
          editType == Property.EDIT_TYPE_RADIOBUTTONS_HORIZONTAL, label);
      case EDIT_TYPE_COMBOBOX:
        return this.createComboBox(label);
      case EDIT_TYPE_LIST:
        return this.createList(label);
      case EDIT_TYPE_SPINNER:
        return this.createSpinner(label);
      case EDIT_TYPE_NUMBERFIELD:
        return this.createTextField(10, SwingConstants.RIGHT, label);
      default:
        return this.createTextField(20, SwingConstants.LEFT, label);
    }
  }


  protected JComponent createRadioButtons(final boolean horizontal,
      boolean label) {

    JPanel buttons = new JPanel() {

      boolean updating = false;

      private T[] values = Property.this.getPossibleValues();
      private ButtonGroup buttonGroup = new ButtonGroup();
      private Map<T, AbstractButton> buttonMap =
        new HashMap<T, AbstractButton>();

      ChangeListener l = new ChangeListener() {

        public void stateChanged(ChangeEvent evt) {

          if (Property.this.getPossibleValues() != values) {
            initOptions();
          }
          if (!updating) {
            updating = true;
            buttonMap.get(Property.this.getValueAsObject()).setSelected(true);

            for (AbstractButton b : buttonMap.values()) {
              b.setEnabled(Property.this.isEditable());
            }
            updating = false;
          }
        }
      };


      @Override
      public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        for (int i = 0; i < this.getComponentCount(); i++) {
          this.getComponent(i).setEnabled(enabled);
        }
      }


      @Override
      public void addNotify() {

        super.addNotify();

        this.initOptions();
        Property.this.addChangeListener(this.l);
      }


      @Override
      public void removeNotify() {

        Property.this.removeChangeListener(this.l);
        super.removeNotify();
      }


      private void initOptions() {

        this.removeAll();
        Collection<AbstractButton> oldButtons = new ArrayList<AbstractButton>(
                    this.buttonGroup.getButtonCount());
        for (Enumeration<AbstractButton> e = this.buttonGroup.getElements(); e
          .hasMoreElements();) {
          oldButtons.add(e.nextElement());
        }
        for (AbstractButton b : oldButtons) {
          this.buttonGroup.remove(b);
        }
        this.buttonMap.clear();

        if (horizontal) {
          this.setLayout(new GridLayout(1, 0, 6, 6));
        }
        else {
          this.setLayout(new GridLayout(0, 1, 6, 6));
        }

        if (this.values != null) {
          boolean nullAllowed = false;
          for (int i = 0; i < this.values.length; i++) {
            if (this.values[i] == null) {
              nullAllowed = true;
              break;
            }
          }

          Collection<T> selectableValues =
            new ArrayList<T>(this.values.length + 1);
          selectableValues.addAll(Arrays.asList(this.values));
          if (!nullAllowed) {
            selectableValues.add(null);
          }

          T value = Property.this.getValueAsObject();
          for (final T source : selectableValues) {
            final JRadioButton b =
              new JRadioButton(source == null ? (String)null
                                : source.toString());
            b
              .setSelected(source == null ? value == null : source
                .equals(value));
            b.addActionListener(new ActionListener() {

              public void actionPerformed(ActionEvent evt) {

                if (b.isSelected() && !updating) {
                  updating = true;
                  Property.this.setValue(source);
                  updating = false;
                }
              }
            });

            if (!((source == null) && !nullAllowed)) {
              this.add(b);
            }
            else {
              b.addNotify();
            }
            this.buttonGroup.add(b);
            this.buttonMap.put(source, b);
          }
        }
        for (AbstractButton b : this.buttonMap.values()) {
          b.setEnabled(Property.this.isEditable());
        }
      }
    };

    if (label) {
      JPanel p = new PPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.NORTH;
      gbc.weightx = 0.0;
      gbc.weighty = 1.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(buttons);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 6, 0, 0);
      p.add(buttons, gbc);
      return p;
    }
    else {
      return buttons;
    }
  }


  protected JComponent createTextField(int size, int alignment, boolean label) {

    JTextField f = new PTextField(this, size);
    f.setHorizontalAlignment(alignment);
    f.setMinimumSize(f.getPreferredSize());

    if (label) {
      JPanel p = new PPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weightx = 0.0;
      gbc.weighty = 0.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(f);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 6, 0, 0);
      p.add(f, gbc);
      return p;
    }
    else {
      return f;
    }
  }


  protected JComponent createList(boolean label) {

    final PList<T> list = new PList<T>(this);

    JScrollPane jsp =
      new JScrollPane(list, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
        {

          @Override
          public void setEnabled(boolean enabled) {

            super.setEnabled(enabled);
            JViewport viewport = this.getViewport();
            if (viewport.getView() != null) {
              viewport.getView().setEnabled(enabled);
            }
          }
        };
    if (label) {
      JPanel p = new PPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.NONE;
      gbc.anchor = GridBagConstraints.NORTH;
      gbc.weightx = 0.0;
      gbc.weighty = 1.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(list);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.fill = GridBagConstraints.BOTH;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 6, 0, 0);
      p.add(jsp, gbc);
      return p;
    }
    else {
      return jsp;
    }
  }


  protected JComponent createComboBox(boolean label) {

    PComboBox<T> cb = new PComboBox<T>(this);

    if (label) {
      JPanel p = new PPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weightx = 0.0;
      gbc.weighty = 0.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(cb);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 6, 0, 0);
      p.add(cb, gbc);
      return p;
    }
    else {
      return cb;
    }
  }


  protected JComponent createSpinner(boolean label) {

    final PSpinner<T> spinner = new PSpinner<T>(this);

    if (label) {
      JPanel p = new PPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.gridx = 0;
      gbc.gridy = 0;
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.anchor = GridBagConstraints.WEST;
      gbc.weightx = 0.0;
      gbc.weighty = 0.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(spinner);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      gbc.insets = new Insets(0, 6, 0, 0);
      p.add(spinner, gbc);
      return p;
    }
    else {
      return spinner;
    }
  }


  public JMenuItem createMenuItem() {

    return new CmdMenuItem(this.getName(), 1, null, new MenuCommander() {

      public String menuItemName(int cmd, String oldName) {

        return Property.this.getName();
      }


      public boolean menuItemState(int cmd) {

        return Property.this.isEditable();
      }


      public boolean doCommand(int cmd) {

        OptionPane.message(null, Property.this.createEditor(true),
          Property.this.getName(),
                    OptionPane.QUESTION);

        return true;
      }
    });
  }


  public static JPanel createPropertyPanel(Property<?>[] properties,
      boolean fillHorizontally) {

    return new PropertySet<Property<?>>(properties)
      .createPropertyPanel(fillHorizontally);
  }


  public static JPanel createPropertyPanel(
      Collection<? extends Property<?>> properties,
            boolean fillHorizontally) {

    return new PropertySet<Property<?>>(properties)
      .createPropertyPanel(fillHorizontally);
  }


  public static void fillPropertyPanel(JPanel p, GridBagConstraints gbc,
            Property<?>[] properties, boolean fillHorizontally) {

    if (properties == null) {
      throw new IllegalArgumentException();
    }

    new PropertySet<Property<?>>(properties).fillPropertyPanel(p, gbc,
      fillHorizontally);
  }

}