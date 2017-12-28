/*
 * @(#)AbstractLongCallable.java
 * Created on 19.07.2007 by dabo
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

package com.clt.util;

import java.util.List;
import java.util.Vector;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;

/**
 * @author dabo
 * 
 */
public abstract class AbstractLongCallable<V>
    implements LongCallable<V>, Cancellable {

  private List<ProgressListener> listeners = new Vector<ProgressListener>();


  public void addProgressListener(ProgressListener l) {

    this.listeners.add(l);
  }


  public void removeProgressListener(ProgressListener l) {

    this.listeners.remove(l);
  }


  public abstract String getDescription();


  protected abstract V call(ProgressListener l)
      throws Exception;


  final public V call()
      throws Exception {

    return this.call(new ProgressListener() {

      public void progressChanged(ProgressEvent e) {

        for (ProgressListener l : AbstractLongCallable.this.listeners) {
          l.progressChanged(e);
        }
      }
    });
  }


  public void cancel() {

  }


  public boolean canCancel() {

    return false;
  }


  public String getCancelConfirmationPrompt() {

    return null;
  }
}