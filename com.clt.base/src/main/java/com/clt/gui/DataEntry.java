/*
 * @(#)DataEntry.java
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

package com.clt.gui;

import com.clt.event.DataEntryListener;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public interface DataEntry {

  public void requestFocus();


  public boolean dataEntered();


  public void addDataEntryListener(DataEntryListener listener);


  public void removeDataEntryListener(DataEntryListener listener);
}
