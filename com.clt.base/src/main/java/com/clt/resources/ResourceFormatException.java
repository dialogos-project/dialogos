/*
 * @(#)ResourceFormatException.java
 * Created on 11.04.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.resources;

/**
 * @author dabo
 * 
 */
public class ResourceFormatException extends RuntimeException {

  public ResourceFormatException() {

    this("Failed to load a resource");
  }


  public ResourceFormatException(String message) {

    super(message);
  }
}
