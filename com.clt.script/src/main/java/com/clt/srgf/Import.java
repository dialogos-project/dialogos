/*
 * @(#)Import.java
 * Created on 25.01.2007 by dabo
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

package com.clt.srgf;

/**
 * @author dabo
 * 
 */
public class Import {

  private String grammar;
  private String rule;


  public Import(String grammar, String rule) {

    if (grammar == null) {
      throw new IllegalArgumentException();
    }
    if (rule == null) {
      throw new IllegalArgumentException();
    }

    this.grammar = grammar;
    this.rule = rule;
  }


  public String getGrammarName() {

    return this.grammar;
  }


  public String getRuleName() {

    return this.rule;
  }


  public boolean isWildcard() {

    return this.rule.equals("*");
  }


  @Override
  public int hashCode() {

    return this.grammar.hashCode() ^ this.rule.hashCode();
  }


  @Override
  public boolean equals(Object o) {

    if (o instanceof Import) {
      Import i = (Import)o;
      return this.grammar.equals(i.grammar) && this.rule.equals(i.rule);
    }
    else {
      return false;
    }
  }


  @Override
  public String toString() {

    return this.grammar + "." + this.rule;
  }
}
