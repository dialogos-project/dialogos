/*
 * @(#)Tag.java
 * Created on Tue Oct 08 2002
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

package com.clt.srgf;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

class TerminalToken
    extends Token<String> {

  private String pattern;


  public TerminalToken(String pattern) {

    super(pattern.intern());
    this.pattern = pattern;
  }


  @Override
  public boolean match(Input input) {

    if (!input.isEmpty()) {
      return this.pattern.equals(input.getFirstPattern());
    }
    else {
      return false;
    }
  }


  @Override
  public String toString() {

    return this.pattern;
  }
}