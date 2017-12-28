/*
 * @(#)Token.java
 * Created on Thu Dec 12 2002
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

import java.util.Set;

/**
 * The base class for all tokens. Tokens are parser-internal objects, that keep
 * track of the words that any expansion can start with. These tokens are used
 * to optimize the parse by cutting off branches whose token list does not
 * contain the first word of the input string.
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

abstract class Token<T> {

  private T source;


  Token(T source) {

    this.source = source;
  }


  protected T getSource() {

    return this.source;
  }


  @Override
  public final boolean equals(Object o) {

    if (o instanceof Token) {
      return ((Token)o).source == this.source;
    }
    else {
      return false;
    }
  }


  @Override
  public final int hashCode() {

    return this.source.hashCode();
  }


  abstract boolean match(Input input);


  @Override
  public String toString() {

    return this.source.toString();
  }


  static boolean onlyEmptyToken(Set<? extends Token<?>> tokens,
      boolean removeTags) {

    boolean containsEmpty = false;
    for (Token<?> t : tokens) {
      if (t == Token.EMPTY) {
        containsEmpty = true;
      }
      else if ((t == Token.TAG) && removeTags) {
        containsEmpty = true;
      }
      else if (t instanceof Rule.RuleToken) {
        if (((Rule.RuleToken)t).getSource().isClass()) {
          return false;
        }
      }
      else {
        return false;
      }
    }
    return containsEmpty;
  }

  public static TagToken TAG = new TagToken();
  public static EmptyToken EMPTY = new EmptyToken();

  private static class EmptyToken
        extends Token<Object> {

    EmptyToken() {

      super(new Object());
    }


    @Override
    public boolean match(Input input) {

      return true;
    }


    @Override
    public String toString() {

      return "<empty>";
    }
  }

  private static class TagToken
        extends EmptyToken {

    TagToken() {

    }


    @Override
    public String toString() {

      return "<tag>";
    }
  }
}
