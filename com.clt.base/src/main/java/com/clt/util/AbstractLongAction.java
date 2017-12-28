package com.clt.util;

import java.util.List;
import java.util.Vector;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;

public abstract class AbstractLongAction
    implements LongAction, Cancellable {

  private List<ProgressListener> listeners = new Vector<ProgressListener>();


  public void addProgressListener(ProgressListener l) {

    this.listeners.add(l);
  }


  public void removeProgressListener(ProgressListener l) {

    this.listeners.remove(l);
  }


  public abstract String getDescription();


  protected abstract void run(ProgressListener l)
      throws Exception;


  final public void run()
      throws Exception {

    this.run(new ProgressListener() {

      public void progressChanged(ProgressEvent e) {

        for (ProgressListener l : AbstractLongAction.this.listeners) {
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