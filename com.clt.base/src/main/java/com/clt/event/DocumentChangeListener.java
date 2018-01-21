package com.clt.event;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * This is a helper class that combines all methods of a
 * {@link DocumentListener}. This is for the lazy ones that want to implement
 * only one method instead of three. Useful if you are only interested in the
 * fact that a {@link javax.swing.text.Document} changed, without the need to
 * differentiate between text insertion, deletion and replacement.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public abstract class DocumentChangeListener implements DocumentListener {

  /**
   * Simply forward the event to {@link #documentChanged}
   * 
   * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.DocumentEvent)
   */
  public void insertUpdate(DocumentEvent evt) {

    this.documentChanged(evt);
  }


  /**
   * Simply forward the event to {@link #documentChanged}
   * 
   * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.DocumentEvent)
   */
  public void removeUpdate(DocumentEvent evt) {

    this.documentChanged(evt);
  }


  /**
   * Simply forward the event to {@link #documentChanged}
   * 
   * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.DocumentEvent)
   */
  public void changedUpdate(DocumentEvent evt) {

    this.documentChanged(evt);
  }


  /**
   * This method is called whenever the document changes in any way.
   * 
   * @param evt
   */
  public abstract void documentChanged(DocumentEvent evt);
}
