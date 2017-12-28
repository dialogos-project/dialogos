/*
 * @(#)DeviceListener.java
 * Created on Thu Dec 12 2002
 *
 * Copyright (c) 2002 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

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
