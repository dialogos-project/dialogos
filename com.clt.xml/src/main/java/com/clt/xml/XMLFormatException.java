/*
 * @(#)XMLFormatException.java
 * Created on Wed Oct 29 2003
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

package com.clt.xml;

import java.io.IOException;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class XMLFormatException
    extends IOException {

  public XMLFormatException(String message) {

    super(message);
  }


  public XMLFormatException(String message, Throwable cause) {

    this(message);

    this.initCause(cause);
  }
}
