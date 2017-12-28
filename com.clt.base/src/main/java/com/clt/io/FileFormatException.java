/*
 * @(#)FileFormatException.java
 *
 * Copyright (c) 2001 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.io;

import java.io.IOException;

/**
 * Signals that a file could not be read because it is in the wrong format.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class FileFormatException
    extends IOException {

  /**
   * Construct an exception with no further information.
   */
  public FileFormatException() {

    super();
  }


  /**
   * Construct an exception with a description of the format error.
   * 
   * @param message
   *          a description of the format error
   */
  public FileFormatException(String message) {

    super(message);
  }
}
