package com.clt.dialog.client;

/**
 * @author Daniel Bobbert
 * @version 6.0
 */

public interface DeviceListener {

  public void stateChanged(DeviceEvent evt);


  public void dataSent(DeviceEvent evt);


  public void dataReceived(DeviceEvent evt);


  public void dataLogged(DeviceEvent evt);
}
