package com.clt.dialog.client;

import java.util.EventObject;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class DeviceEvent extends EventObject {

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
