/*
 * @(#)DataEntryListener.java
 * Created on Tue Sep 16 2003
 *
 * Copyright (c) 2003 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.event;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface DataEntryListener {

  /** Called when the data of a data entry has changed */
  public void dataChanged(DataEntryEvent evt);
}
