/*
 * @(#)Timeout.java
 * Created on Tue Nov 12 2002
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

package com.clt.util;

/**
 * A <code>Timeout</code> indicates that the timeout limit wait()ing for an
 * operation (e.g. retrieval from a Queue) has been reached.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class Timeout
    extends Exception {

  public Timeout() {

    super();
  }


  public Timeout(String message) {

    super(message);
  }
}
