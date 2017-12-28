/*
 * @(#)LongCallable.java
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

import java.util.concurrent.Callable;

import com.clt.event.ProgressListener;

/**
 * @author dabo
 * 
 */
public interface LongCallable<V>
    extends Callable<V> {

  /** Returns a description of this action */
  public String getDescription();


  /** Register the given ProgressListener */
  public void addProgressListener(ProgressListener l);


  /** Unregister the given ProgressListener */
  public void removeProgressListener(ProgressListener l);
}
