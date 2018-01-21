package com.clt.util;

import com.clt.event.ProgressListener;

/**
 * LongAction is an interface for actions that will take a long time to run.
 * Instances of LongAction must provide a description and a way to register
 * ProgressListeners.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface LongAction {

  /** Returns a description of this action */
  public String getDescription();


  /** Register the given ProgressListener */
  public void addProgressListener(ProgressListener l);


  /** Unregister the given ProgressListener */
  public void removeProgressListener(ProgressListener l);


  public void run()
      throws Exception;
}