/*
 * @(#)TypeException.java
 * Created on Wed Nov 27 2002
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

package com.clt.script.exp;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 2.0
 */

public class TypeException extends RuntimeException {

  public TypeException() {

    super();
  }


  public TypeException(String s) {

    super(s);
  }


  @Override
  public String toString() {

    return "Type error: " + this.getLocalizedMessage();
  }
}
