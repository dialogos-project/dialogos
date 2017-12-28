/*
 * @(#)Connector.java
 * Created on Tue Jun 08 2004
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

import java.io.IOException;
import java.net.InetAddress;

import com.clt.properties.Property;
import com.clt.script.exp.Value;

/**
 * Subclasses of Connector must have a default constructor that initializes the
 * connector's properties to sensible values.
 * 
 * @author Daniel Bobbert
 * @version 6.1
 */

public interface Connector {

  public Object getHelp();


  public Property<?>[] getProperties();


  public String getName();


  public String getInfo();


  public String getDescription();


  public String open(long timeout, TargetSelector selector)
      throws IOException;


  public void close()
      throws IOException;


  public InetAddress getTargetAddress();


  public int getPort();


  public String getTarget();


  public ConnectionState getState();


  public void addDeviceListener(DeviceListener l);


  public void removeDeviceListener(DeviceListener l);


  public void start()
      throws IOException;


  public void reset()
      throws IOException;


  public void allowTimeout(boolean allow)
      throws IOException;


  public void signalTimeout()
      throws IOException;


  public void send(Value value)
      throws IOException;


  public Value rpc(String procedure, Value[] arguments)
      throws IOException;


  public void pushInput(Value value);


  public void echo()
      throws IOException;


  public Value receive()
      throws IOException, InterruptedException;


  public Connector copy();
}
