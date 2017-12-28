/*
 * @(#)PTextField.java
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

import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import com.clt.event.DocumentChangeListener;
import com.clt.properties.Property;

/**
 * @author dabo
 * 
 */
public class PTextField
    extends JTextField {

  private Property<?> property;
  private boolean updating = false;
  private boolean setup = false;


  public PTextField(Property<?> property, int size) {

    super(size);

    this.property = property;
  }

  ChangeListener l = new ChangeListener() {

    public void stateChanged(ChangeEvent evt) {

      if (!PTextField.this.updating) {
        PTextField.this.updating = true;
        PTextField.this.setText(PTextField.this.property.getValueAsString());
        PTextField.this.setEditable(PTextField.this.property.isEditable());
        PTextField.this.updating = false;
      }
    }
  };

  DocumentListener dl = new DocumentChangeListener() {

    @Override
    public void documentChanged(DocumentEvent evt) {

      if (!PTextField.this.updating) {
        PTextField.this.updating = true;
        try {
          PTextField.this.property
            .setValueFromString(PTextField.this.getText());
        }
                catch (Exception ignore) {
                  // TODO: Maybe we should somehow mark the field
                  // to signal that the current value is invalid
                }
                PTextField.this.updating = false;
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
    this.property.addChangeListener(this.l);
    if (!this.setup) {
      Document doc = this.getDocument();
      if (doc != null) {
        doc.addDocumentListener(this.dl);
      }
      this.setup = true;
    }
    this.l.stateChanged(new ChangeEvent(this.property));
  }


  @Override
  public void removeNotify() {

    this.property.removeChangeListener(this.l);
    if (this.setup) {
      Document doc = this.getDocument();
      if (doc != null) {
        doc.removeDocumentListener(this.dl);
      }
      this.setup = false;
    }
    super.removeNotify();
  }
}
