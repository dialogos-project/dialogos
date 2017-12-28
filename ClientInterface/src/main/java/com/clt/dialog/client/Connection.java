/*
 * @(#)Connection.java
 * Created on 22.05.2006 by dabo
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

package com.clt.dialog.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

interface Connection {

  public InetAddress getTargetAddress();


  public int getPort();


  public InputStream getInputStream()
      throws IOException;


  public OutputStream getOutputStream()
      throws IOException;


  public void close()
      throws IOException;


  public int getProtocol();
}