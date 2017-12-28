/*
 * @(#)BrickException.java
 * Created on 04.06.2007 by dabo
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

package com.clt.lego;

import java.io.IOException;

/**
 * @author dabo
 * 
 */
public class BrickException
    extends IOException {

  public BrickException(String message) {

    super(message);
  }


  public BrickException(String message, Throwable cause) {

    this(message);

    this.initCause(cause);
  }
}
