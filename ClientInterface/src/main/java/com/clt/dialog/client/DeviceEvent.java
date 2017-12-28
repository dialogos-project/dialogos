/*
 * @(#)DeviceEvent.java
 * Created on Wed Aug 11 2004
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.dialog.client;

import java.util.EventObject;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DeviceEvent
    extends EventObject {

  /** The current state of the device. */
  private ConnectionState state;

  private Object data;


  public DeviceEvent(Object source, ConnectionState state) {

    this(source, state, null);
  }


  public DeviceEvent(Object source, ConnectionState state, Object data) {

    super(source);
    this.state = state;
    this.data = data;
  }


  public ConnectionState getState() {

    return this.state;
  }


  public Object getData() {

    return this.data;
  }
}
