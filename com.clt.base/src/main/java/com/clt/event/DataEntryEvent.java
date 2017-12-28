/*
 * @(#)DataEntryEvent.java
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

import java.util.EventObject;

import com.clt.gui.DataEntry;

/**
 * A semantic event that the data in some data entry has changed.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DataEntryEvent
    extends EventObject {

  /**
   * Construct a new event from the given source.
   * 
   * @param source
   */
  public DataEntryEvent(DataEntry source) {

    super(source);
  }


  /**
   * Get the source of this event.
   */
  public DataEntry getDataEntry() {

    return (DataEntry)this.getSource();
  }
}
