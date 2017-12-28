/*
 * @(#)DefaultLongAction.java
 * Created on 16.04.2007 by dabo
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

import com.clt.event.ProgressListener;

/**
 * @author dabo
 */
public abstract class DefaultLongAction extends AbstractLongAction {

  private String description;


  /**
   * Constructor. Takes a description as argument.
   * 
   * @param description
   *          Description of the executed action
   */
  public DefaultLongAction(String description) {

    this.description = description;
  }


  @Override
  protected abstract void run(ProgressListener l)
      throws Exception;


  @Override
  public String getDescription() {

    return this.description;
  }


  public static <T> LongCallable<T> asCallable(final LongAction action) {

    return new AbstractLongCallable<T>() {

      @Override
      public void cancel() {

        if (action instanceof Cancellable) {
          ((Cancellable)action).cancel();
        }
      }


      @Override
      public boolean canCancel() {

        if (action instanceof Cancellable) {
          return ((Cancellable)action).canCancel();
        }
        else {
          return false;
        }
      }


      @Override
      protected T call(ProgressListener l)
          throws Exception {

        action.addProgressListener(l);
        action.run();
        action.removeProgressListener(l);
        return null;
      }


      @Override
      public String getDescription() {

        return action.getDescription();
      }
    };
  }
}
