/*
 * @(#)SemanticException.java
 * Created on Tue Oct 15 2002
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
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class SemanticException extends RuntimeException {

  private Grammar grammar;


  public SemanticException(Grammar grammar) {

    super();

    if (grammar == null) {
      throw new IllegalArgumentException();
    }
    this.grammar = grammar;
  }


  public SemanticException(Grammar grammar, String s) {

    super(s);

    if (grammar == null) {
      throw new IllegalArgumentException();
    }
    this.grammar = grammar;
  }


  @Override
  public final String getMessage() {

    String prefix = "";
    if ((this.grammar != null) && (this.grammar.getName() != null)) {
      prefix = "Error in grammar " + this.grammar.getName() + ": ";
    }
    return prefix + super.getMessage();
  }
}
