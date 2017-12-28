/*
 * @(#)ColorProperty.java
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.clt.event.DocumentChangeListener;
import com.clt.gui.FileChooser;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class FileProperty
    extends Property<File> {

  public FileProperty(String id) {

    super(id, Property.EDIT_TYPE_FILE_CHOOSER);
  }


  public abstract File getValue();


  protected void setValueImpl(File value) {

    // the default implementation does nothing
  }


  @Override
  public final void setValue(File value) {

    if (value != this.getValue()) {
      this.setValueImpl(value);
      this.fireChange();
    }
  }


  @Override
  public String getValueAsString() {

    File f = this.getValue();
    if (f == null) {
      return null;
    }
    else {
      return f.toString();
    }
  }


  @Override
  public File getValueAsObject() {

    return this.getValue();
  }


  @Override
  public void setValueFromString(String s) {

    this.setValue(new File(s));
  }


  @Override
  public File[] getPossibleValues() {

    return null;
  }


  @Override
  protected int getSupportedEditTypesImpl() {

    return Property.EDIT_TYPE_FILE_CHOOSER;
  }


  @Override
  protected JComponent createEditorComponent(int editType, boolean label) {

    switch (editType) {
      case EDIT_TYPE_FILE_CHOOSER:
        return this.createFileChooser(16, label);
      default:
        return super.createEditorComponent(editType, label);
    }
  }


  protected JComponent createFileChooser(int size, boolean label) {

    JTextField f = new JTextField(size) {

      boolean updating = false;

      boolean setup = false;

      ChangeListener l = new ChangeListener() {

        public void stateChanged(ChangeEvent evt) {

          if (!updating) {
            updating = true;
            setText(FileProperty.this.getValueAsString());
            setEnabled(FileProperty.this.isEditable());
            updating = false;
          }
        }
      };

      DocumentListener dl = new DocumentChangeListener() {

        @Override
        public void documentChanged(DocumentEvent evt) {

          if (!updating) {
            updating = true;
            try {
              FileProperty.this.setValueFromString(getText());
            }
                        catch (Exception exn) {
                          // TODO: Maybe we could mark the field somehow to
                          // signal that the current value is invalid?!
                        }
                        updating = false;
                      }
                    }
      };


      @Override
      public void setDocument(Document doc) {

        Document oldDoc = this.getDocument();
        if ((oldDoc != null) && this.setup) {
          oldDoc.removeDocumentListener(this.dl);
        }

        if (doc != null) {
          doc.addDocumentListener(this.dl);
        }
        this.setup = true;
        super.setDocument(doc);
      }


      @Override
      public void addNotify() {

        super.addNotify();
        FileProperty.this.addChangeListener(this.l);
        if (!this.setup) {
          Document doc = this.getDocument();
          if (doc != null) {
            doc.addDocumentListener(this.dl);
          }
          this.setup = true;
        }
        this.l.stateChanged(new ChangeEvent(FileProperty.this));
      }


      @Override
      public void removeNotify() {

        FileProperty.this.removeChangeListener(this.l);
        super.removeNotify();
        if (this.setup) {
          Document doc = this.getDocument();
          if (doc != null) {
            doc.removeDocumentListener(this.dl);
          }
          this.setup = false;
        }
      }
    };
    f.setMinimumSize(f.getPreferredSize());

    final JButton b = new JButton("...") {

      ChangeListener l = new ChangeListener() {

        public void stateChanged(ChangeEvent evt) {

          setEnabled(FileProperty.this.isEditable());
        }
      };


      @Override
      public void addNotify() {

        super.addNotify();
        FileProperty.this.addChangeListener(this.l);
        this.l.stateChanged(new ChangeEvent(FileProperty.this));
      }


      @Override
      public void removeNotify() {

        FileProperty.this.removeChangeListener(this.l);
        super.removeNotify();
      }
    };
    b.addActionListener(new ActionListener() {

      FileChooser fc = null;


      public void actionPerformed(ActionEvent evt) {

        if (this.fc == null) {
          this.fc = new FileChooser();
        }
        File file = this.fc.standardGetFile(b);
        if (file != null) {
          FileProperty.this.setValue(file);
        }
      }
    });

    JPanel p = new JPanel(new GridBagLayout()) {

      @Override
      public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        for (int i = 0; i < this.getComponentCount(); i++) {
          this.getComponent(i).setEnabled(enabled);
        }
      }
    };
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.weighty = 0.0;

    if (label) {
      gbc.weightx = 0.0;
      JLabel l = new JLabel(this.getName());
      l.setLabelFor(f);
      p.add(l, gbc);
      gbc.gridx++;
      gbc.insets = new Insets(0, 6, 0, 0);
    }
    gbc.weightx = 1.0;
    p.add(f, gbc);
    gbc.gridx++;
    gbc.insets = new Insets(0, 6, 0, 0);
    gbc.weightx = 0.0;
    p.add(b, gbc);
    return p;
  }
}